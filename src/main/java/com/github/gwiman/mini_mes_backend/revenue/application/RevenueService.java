package com.github.gwiman.mini_mes_backend.revenue.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.AvailableOrderLineResponse;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueCreateRequest;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueResponse;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueUpdateRequest;
import com.github.gwiman.mini_mes_backend.revenue.domain.Revenue;
import com.github.gwiman.mini_mes_backend.revenue.domain.RevenueLine;
import com.github.gwiman.mini_mes_backend.revenue.domain.RevenueRepository;
import com.github.gwiman.mini_mes_backend.revenue.internal.RevenueQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 매출 서비스.
 * 담당자가 거래처를 선택하고 해당 거래처의 완료 수주 품목을 골라 수동으로 매출을 생성한다.
 * 초안→마감/취소 상태 흐름을 관리하며, 초안 상태에서만 수정·삭제가 허용된다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueService {

	private static final String REVENUE_NUMBER_PREFIX = "RE_";
	private static final String STATUS_DRAFT     = "REVENUE_STATUS_01";
	private static final String STATUS_CLOSED    = "REVENUE_STATUS_02";

	private final RevenueRepository revenueRepository;
	private final RevenueQueryRepository revenueQueryRepository;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<RevenueResponse> findAll(String statusCode, Long partnerId,
		Long salesOrderId, LocalDate fromDate, LocalDate toDate) {
		return revenueQueryRepository.search(statusCode, partnerId, salesOrderId, fromDate, toDate);
	}

	public RevenueResponse findById(Long id) {
		return revenueQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("매출을 찾을 수 없습니다: " + id));
	}

	/**
	 * 거래처의 완료 수주에서 선택 가능한 수주 라인 목록을 반환한다.
	 * 매출 생성 팝업에서 품목 선택 시 사용된다.
	 */
	public List<AvailableOrderLineResponse> findAvailableOrderLines(Long partnerId) {
		return revenueQueryRepository.findAvailableOrderLines(partnerId);
	}

	/**
	 * 매출을 생성한다.
	 * 거래처 및 선택된 수주 라인 목록을 기반으로 채번 후 저장한다.
	 */
	@Transactional
	public RevenueResponse create(RevenueCreateRequest request) {
		String revenueNumber = documentNumberGenerator.generateRaw(
			REVENUE_NUMBER_PREFIX, "revenue", "revenue_number");

		Revenue revenue = new Revenue(
			revenueNumber,
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getRevenueDate(),
			STATUS_DRAFT,
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		int sortOrder = 0;
		for (RevenueCreateRequest.LineItem lineReq : request.getLines()) {
			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			RevenueLine line = new RevenueLine(
				revenue,
				lineReq.getSalesOrderLineId(),
				lineReq.getSalesOrderId(),
				lineReq.getItemId(),
				lineReq.getQuantity(),
				lineReq.getUnitPrice(),
				amount,
				lineReq.getRemarks() != null ? lineReq.getRemarks() : "",
				sortOrder++
			);
			revenue.addLine(line);
		}

		Revenue saved = revenueRepository.save(revenue);
		return revenueQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 매출을 조회할 수 없습니다: " + saved.getId()));
	}

	/**
	 * 매출을 수정한다.
	 * 초안 상태에서만 허용되며, 라인별 수량·단가를 변경할 수 있다.
	 */
	@Transactional
	public RevenueResponse update(Long id, RevenueUpdateRequest request) {
		Revenue revenue = revenueRepository.findWithLinesById(id)
			.orElseThrow(() -> new ResourceNotFoundException("매출을 찾을 수 없습니다: " + id));

		if (!STATUS_DRAFT.equals(revenue.getStatusCode())) {
			throw new BusinessRuleViolationException("초안 상태에서만 수정할 수 있습니다.");
		}

		revenue.update(
			request.getEmployeeId(),
			request.getRevenueDate(),
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		Map<Long, RevenueUpdateRequest.LineItem> lineMap = request.getLines().stream()
			.collect(Collectors.toMap(RevenueUpdateRequest.LineItem::getId, l -> l));

		for (RevenueLine line : revenue.getLines()) {
			RevenueUpdateRequest.LineItem lineReq = lineMap.get(line.getId());
			if (lineReq != null) {
				line.update(
					lineReq.getQuantity(),
					lineReq.getUnitPrice(),
					lineReq.getRemarks() != null ? lineReq.getRemarks() : ""
				);
			}
		}

		return revenueQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 매출을 조회할 수 없습니다: " + id));
	}

	/**
	 * 매출을 마감 처리한다.
	 * 초안 상태에서만 마감으로 전환할 수 있다.
	 */
	@Transactional
	public RevenueResponse close(Long id) {
		Revenue revenue = revenueRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("매출을 찾을 수 없습니다: " + id));

		if (!STATUS_DRAFT.equals(revenue.getStatusCode())) {
			throw new BusinessRuleViolationException("초안 상태에서만 마감할 수 있습니다.");
		}

		revenue.close();
		return revenueQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 매출을 조회할 수 없습니다: " + id));
	}

	/**
	 * 매출을 취소 처리한다.
	 * 마감 상태에서만 취소로 전환할 수 있다.
	 */
	@Transactional
	public RevenueResponse cancel(Long id) {
		Revenue revenue = revenueRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("매출을 찾을 수 없습니다: " + id));

		if (!STATUS_CLOSED.equals(revenue.getStatusCode())) {
			throw new BusinessRuleViolationException("마감 상태에서만 취소할 수 있습니다.");
		}

		revenue.cancel();
		return revenueQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 매출을 조회할 수 없습니다: " + id));
	}

	/**
	 * 매출을 삭제한다.
	 * 초안 상태에서만 삭제 가능하다.
	 */
	@Transactional
	public void delete(Long id) {
		Revenue revenue = revenueRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("매출을 찾을 수 없습니다: " + id));

		if (!STATUS_DRAFT.equals(revenue.getStatusCode())) {
			throw new BusinessRuleViolationException("초안 상태에서만 삭제할 수 있습니다.");
		}

		revenueRepository.deleteById(id);
	}
}
