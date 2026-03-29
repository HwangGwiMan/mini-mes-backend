package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 매출 수정 요청 DTO.
 * 초안 상태에서만 허용되며, 라인별 수량/단가를 수정할 수 있다.
 */
public record RevenueUpdateRequest(
	Long employeeId,

	@NotNull
	LocalDate revenueDate,

	String remarks,

	@Valid
	@NotEmpty
	List<LineItem> lines
) {
	public record LineItem(
		@NotNull Long id,
		@NotNull BigDecimal quantity,
		@NotNull BigDecimal unitPrice,
		String remarks
	) {}
}
