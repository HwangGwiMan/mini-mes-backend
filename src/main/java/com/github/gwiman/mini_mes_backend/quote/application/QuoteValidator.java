package com.github.gwiman.mini_mes_backend.quote.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteLineRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteRequest;

import lombok.RequiredArgsConstructor;

/**
 * 견적 저장/수정 시 외래키 존재 여부를 검증하는 컴포넌트.
 * create/update 양쪽에서 동일한 검증이 필요하므로 서비스에서 분리.
 */
@Component
@RequiredArgsConstructor
class QuoteValidator {

	private final PartnerService partnerService;
	private final EmployeeService employeeService;
	private final ItemService itemService;

	/**
	 * 견적 헤더의 참조 데이터(거래처, 담당자, 결재자) 존재 여부 검증.
	 * 담당자는 선택값이므로 null인 경우 건너뜀.
	 */
	void validateHeader(QuoteRequest request) {
		if (!partnerService.exists(request.partnerId())) {
			throw new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + request.partnerId());
		}
		if (request.employeeId() != null && !employeeService.exists(request.employeeId())) {
			throw new ResourceNotFoundException("담당자를 찾을 수 없습니다: " + request.employeeId());
		}
		if (!employeeService.exists(request.approverId())) {
			throw new ResourceNotFoundException("결재자를 찾을 수 없습니다: " + request.approverId());
		}
	}

	/** 견적 라인의 품목 존재 여부를 IN 쿼리 한 번으로 일괄 검증 — 라인 수만큼 쿼리가 발생하는 N+1 방지 */
	void validateLines(List<QuoteLineRequest> lines) {
		Set<Long> requestedIds = lines.stream()
			.map(QuoteLineRequest::itemId)
			.collect(Collectors.toSet());

		Set<Long> existingIds = itemService.findExistingIds(requestedIds);

		requestedIds.stream()
			.filter(id -> !existingIds.contains(id))
			.findFirst()
			.ifPresent(id -> {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + id);
			});
	}
}
