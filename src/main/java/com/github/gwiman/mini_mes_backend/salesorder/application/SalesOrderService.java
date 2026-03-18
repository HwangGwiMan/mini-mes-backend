package com.github.gwiman.mini_mes_backend.salesorder.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteConvertedToOrderEvent;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteHeaderData;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteLineData;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteService;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderLineRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderResponse;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrder;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderLine;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderRepository;
import com.github.gwiman.mini_mes_backend.salesorder.internal.SalesOrderQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesOrderService {

	private static final String ORDER_NUMBER_PREFIX = "SO_";

	private final SalesOrderRepository salesOrderRepository;
	private final SalesOrderQueryRepository salesOrderQueryRepository;
	private final PartnerService partnerService;
	private final EmployeeService employeeService;
	private final ItemService itemService;
	private final QuoteService quoteService;
	private final ApplicationEventPublisher eventPublisher;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<SalesOrderResponse> findAll(String orderNumber, Long partnerId, String statusCode,
		LocalDate fromDate, LocalDate toDate) {
		String orderNumberPattern = QueryParamEscaper.containsLike(orderNumber);
		return salesOrderQueryRepository.search(orderNumberPattern, partnerId, statusCode, fromDate, toDate);
	}

	public SalesOrderResponse findById(Long id) {
		return salesOrderQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("수주를 찾을 수 없습니다: " + id));
	}

	/** 타 모듈(shipment)에서 출하 계획 생성 시 필요한 수주 데이터 반환 — domain 엔티티 직접 노출 방지 */
	public SalesOrderData getOrderWithLines(Long id) {
		SalesOrder order = salesOrderRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("수주를 찾을 수 없습니다: " + id));
		List<SalesOrderData.Line> lines = order.getLines().stream()
			.map(l -> new SalesOrderData.Line(l.getId(), l.getItemId(), l.getQuantity(), l.getUnitPrice(), l.getRemarks()))
			.toList();
		return new SalesOrderData(order.getId(), order.getPartnerId(), order.getEmployeeId(), lines);
	}

	@Transactional
	public SalesOrderResponse create(SalesOrderRequest request) {
		String orderNumber = generateOrderNumber();
		validatePartner(request.getPartnerId());
		validateEmployee(request.getEmployeeId());

		SalesOrder order = SalesOrder.create(orderNumber,
			request.getOrderDate(), request.getDeliveryDate(),
			request.getPartnerId(), request.getEmployeeId(),
			request.getQuoteId(), request.getStatusCode(), request.getRemarks());

		addLines(order, request.getLines());

		SalesOrder saved = salesOrderRepository.save(order);
		// 출하 도메인이 수신해 출하 계획을 자동 생성한다
		eventPublisher.publishEvent(new SalesOrderCreatedEvent(saved.getId()));
		return salesOrderQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 수주를 조회할 수 없습니다: " + saved.getId()));
	}

	@Transactional
	public SalesOrderResponse update(Long id, SalesOrderRequest request) {
		SalesOrder order = salesOrderRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("수주를 찾을 수 없습니다: " + id));

		validatePartner(request.getPartnerId());
		validateEmployee(request.getEmployeeId());

		order.update(
			request.getOrderDate(),
			request.getDeliveryDate(),
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		order.clearLines();
		addLines(order, request.getLines());

		return salesOrderQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 수주를 조회할 수 없습니다: " + id));
	}

	@Transactional
	public void delete(Long id) {
		if (!salesOrderRepository.existsById(id)) {
			throw new ResourceNotFoundException("수주를 찾을 수 없습니다: " + id);
		}
		salesOrderRepository.deleteById(id);
	}

	@Transactional
	public SalesOrderResponse convertFromQuote(Long quoteId) {
		if (salesOrderRepository.existsByQuoteId(quoteId)) {
			throw new BusinessRuleViolationException("이미 수주 전환된 견적입니다: " + quoteId);
		}

		QuoteHeaderData quoteHeader = quoteService.findHeaderById(quoteId);
		if (!"QUOTE_STATUS_03".equals(quoteHeader.statusCode())) {
			throw new BusinessRuleViolationException("승인된 견적만 수주전환이 가능합니다.");
		}
		List<QuoteLineData> quoteLines = quoteService.getLines(quoteId);

		String orderNumber = generateOrderNumber();

		SalesOrder order = SalesOrder.fromQuote(orderNumber, quoteId,
			quoteHeader.partnerId(), quoteHeader.employeeId());

		int sortOrder = 0;
		for (QuoteLineData quoteLine : quoteLines) {
			order.addLine(SalesOrderLine.of(order,
				quoteLine.itemId(), quoteLine.quantity(), quoteLine.unitPrice(),
				quoteLine.deliveryRequestDate(),
				quoteLine.remarks() != null ? quoteLine.remarks() : "", sortOrder++));
		}

		SalesOrder saved = salesOrderRepository.save(order);
		eventPublisher.publishEvent(new QuoteConvertedToOrderEvent(quoteId));
		// 견적 전환 수주도 출하 계획 자동 생성 대상
		eventPublisher.publishEvent(new SalesOrderCreatedEvent(saved.getId()));

		return salesOrderQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 수주를 조회할 수 없습니다: " + saved.getId()));
	}

	private void addLines(SalesOrder order, List<SalesOrderLineRequest> lineRequests) {
		int sortOrder = 0;
		for (SalesOrderLineRequest lineReq : lineRequests) {
			if (!itemService.exists(lineReq.getItemId())) {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + lineReq.getItemId());
			}
			order.addLine(SalesOrderLine.of(order,
				lineReq.getItemId(), lineReq.getQuantity(), lineReq.getUnitPrice(),
				lineReq.getDeliveryRequestDate(),
				lineReq.getRemarks() != null ? lineReq.getRemarks() : "", sortOrder++));
		}
	}

	private void validatePartner(Long partnerId) {
		if (!partnerService.exists(partnerId)) {
			throw new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + partnerId);
		}
	}

	private void validateEmployee(Long employeeId) {
		if (employeeId != null && !employeeService.exists(employeeId)) {
			throw new ResourceNotFoundException("담당자를 찾을 수 없습니다: " + employeeId);
		}
	}

	private String generateOrderNumber() {
		return documentNumberGenerator.generate(
			ORDER_NUMBER_PREFIX,
			com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder.SALES_ORDER.ORDER_NUMBER
		);
	}

}
