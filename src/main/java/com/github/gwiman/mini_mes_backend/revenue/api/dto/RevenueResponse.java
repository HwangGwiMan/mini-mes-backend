package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매출 헤더 응답 DTO.
 * name 필드는 useCrudPage 호환용으로 revenueNumber와 동일한 값을 가진다.
 * totalAmount는 라인 금액 합계이며 목록 조회 시에도 반환된다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponse {

	private Long id;
	private String revenueNumber;
	private String name; // useCrudPage 호환용 (revenueNumber와 동일)
	private Long partnerId;
	private String partnerName;
	private Long employeeId;
	private String employeeName;
	private LocalDate revenueDate;
	private String statusCode;
	private BigDecimal totalAmount;
	private String remarks;
	private List<RevenueLineResponse> lines;
}
