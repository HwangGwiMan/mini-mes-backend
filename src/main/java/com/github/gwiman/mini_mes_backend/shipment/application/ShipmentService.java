package com.github.gwiman.mini_mes_backend.shipment.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.salesorder.application.SalesOrderCreatedEvent;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrder;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderLine;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderRepository;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentCompleteRequest;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentResponse;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentUpdateRequest;
import com.github.gwiman.mini_mes_backend.shipment.domain.Shipment;
import com.github.gwiman.mini_mes_backend.shipment.domain.ShipmentLine;
import com.github.gwiman.mini_mes_backend.shipment.domain.ShipmentRepository;
import com.github.gwiman.mini_mes_backend.shipment.internal.ShipmentQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 출하 서비스.
 * 수주 생성 이벤트를 수신해 출하 계획을 자동 생성하고,
 * 출하 계획 수정 및 출하 완료 처리 비즈니스 로직을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {

	private static final String SHIPMENT_NUMBER_PREFIX = "SH_";
	private static final String STATUS_WAITING = "SHIPMENT_STATUS_01";
	private static final String STATUS_COMPLETED = "SHIPMENT_STATUS_03";

	private final ShipmentRepository shipmentRepository;
	private final ShipmentQueryRepository shipmentQueryRepository;
	private final SalesOrderRepository salesOrderRepository;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<ShipmentResponse> findAll(String statusCode, Long salesOrderId, Long partnerId,
		LocalDate fromDate, LocalDate toDate) {
		return shipmentQueryRepository.search(statusCode, salesOrderId, partnerId, fromDate, toDate);
	}

	public ShipmentResponse findById(Long id) {
		return shipmentQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("출하를 찾을 수 없습니다: " + id));
	}

	/**
	 * 수주 생성 이벤트 수신 시 출하 계획을 자동 생성한다.
	 * 동기 트랜잭션 이벤트이므로 수주 저장과 같은 트랜잭션 내에서 실행된다.
	 */
	@ApplicationModuleListener
	public void on(SalesOrderCreatedEvent event) {
		createFromOrder(event.salesOrderId());
	}

	/**
	 * 수주를 기반으로 출하 계획을 생성한다.
	 * 수주 라인 전체를 출하 라인으로 복사하며 계획수량은 수주 수량과 동일하게 설정한다.
	 */
	@Transactional
	public ShipmentResponse createFromOrder(Long salesOrderId) {
		SalesOrder order = salesOrderRepository.findByIdWithLines(salesOrderId)
			.orElseThrow(() -> new ResourceNotFoundException("수주를 찾을 수 없습니다: " + salesOrderId));

		String shipmentNumber = generateShipmentNumber();

		Shipment shipment = new Shipment(
			shipmentNumber,
			order.getId(),
			order.getPartnerId(),
			order.getEmployeeId(),
			STATUS_WAITING,
			""
		);

		int sortOrder = 0;
		for (SalesOrderLine orderLine : order.getLines()) {
			BigDecimal plannedAmount = orderLine.getQuantity().multiply(orderLine.getUnitPrice());
			ShipmentLine line = new ShipmentLine(
				shipment,
				orderLine.getId(),
				orderLine.getItemId(),
				orderLine.getQuantity(),
				orderLine.getUnitPrice(),
				plannedAmount,
				orderLine.getRemarks() != null ? orderLine.getRemarks() : "",
				sortOrder++
			);
			shipment.addLine(line);
		}

		Shipment saved = shipmentRepository.save(shipment);
		return shipmentQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 출하를 조회할 수 없습니다: " + saved.getId()));
	}

	/**
	 * 출하 계획을 수정한다.
	 * 출하완료 상태에서는 수정할 수 없다.
	 */
	@Transactional
	public ShipmentResponse update(Long id, ShipmentUpdateRequest request) {
		Shipment shipment = shipmentRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("출하를 찾을 수 없습니다: " + id));

		if (STATUS_COMPLETED.equals(shipment.getStatusCode())) {
			throw new BusinessRuleViolationException("출하완료 상태에서는 수정할 수 없습니다.");
		}

		shipment.update(
			request.getEmployeeId(),
			request.getStatusCode(),
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		// 라인별 계획수량 수정
		Map<Long, ShipmentUpdateRequest.LineItem> lineRequestMap = request.getLines().stream()
			.collect(Collectors.toMap(ShipmentUpdateRequest.LineItem::getId, l -> l));

		for (ShipmentLine line : shipment.getLines()) {
			ShipmentUpdateRequest.LineItem lineReq = lineRequestMap.get(line.getId());
			if (lineReq != null) {
				BigDecimal plannedAmount = lineReq.getPlannedQuantity().multiply(line.getUnitPrice());
				line.updatePlan(
					lineReq.getPlannedQuantity(),
					plannedAmount,
					lineReq.getRemarks() != null ? lineReq.getRemarks() : ""
				);
			}
		}

		return shipmentQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 출하를 조회할 수 없습니다: " + id));
	}

	/**
	 * 출하를 완료 처리한다.
	 * 실출하일자를 기록하고 라인별 실출하수량과 실출하금액을 저장한다.
	 */
	@Transactional
	public ShipmentResponse complete(Long id, ShipmentCompleteRequest request) {
		Shipment shipment = shipmentRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("출하를 찾을 수 없습니다: " + id));

		if (STATUS_COMPLETED.equals(shipment.getStatusCode())) {
			throw new BusinessRuleViolationException("이미 출하완료 처리된 출하입니다.");
		}

		shipment.complete(request.getShipmentDate());

		Map<Long, ShipmentCompleteRequest.LineItem> lineRequestMap = request.getLines().stream()
			.collect(Collectors.toMap(ShipmentCompleteRequest.LineItem::getId, l -> l));

		for (ShipmentLine line : shipment.getLines()) {
			ShipmentCompleteRequest.LineItem lineReq = lineRequestMap.get(line.getId());
			if (lineReq != null) {
				BigDecimal actualAmount = lineReq.getActualQuantity().multiply(line.getUnitPrice());
				line.complete(lineReq.getActualQuantity(), actualAmount);
			}
		}

		return shipmentQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 출하를 조회할 수 없습니다: " + id));
	}

	/**
	 * 출하를 삭제한다.
	 * 출하대기 상태에서만 삭제 가능하다.
	 */
	@Transactional
	public void delete(Long id) {
		Shipment shipment = shipmentRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("출하를 찾을 수 없습니다: " + id));

		if (!STATUS_WAITING.equals(shipment.getStatusCode())) {
			throw new BusinessRuleViolationException("출하대기 상태에서만 삭제할 수 있습니다.");
		}

		shipmentRepository.deleteById(id);
	}

	private String generateShipmentNumber() {
		// jOOQ 생성 클래스가 없으므로 원시 DSL 방식으로 채번 — jooqCodegen 후 교체 예정
		return documentNumberGenerator.generateRaw(SHIPMENT_NUMBER_PREFIX, "shipment", "shipment_number");
	}
}
