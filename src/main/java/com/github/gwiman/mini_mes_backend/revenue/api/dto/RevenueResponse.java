package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 매출 헤더 응답 DTO.
 * name 필드는 useCrudPage 호환용으로 revenueNumber와 동일한 값을 가진다.
 * totalAmount는 라인 금액 합계이며 목록 조회 시에도 반환된다.
 */
public record RevenueResponse(
	Long id,
	String revenueNumber,
	String name, // useCrudPage 호환용 (revenueNumber와 동일)
	Long partnerId,
	String partnerName,
	Long employeeId,
	String employeeName,
	LocalDate revenueDate,
	String statusCode,
	BigDecimal totalAmount,
	String remarks,
	List<RevenueLineResponse> lines
) {}
