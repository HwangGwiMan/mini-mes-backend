package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매출 수정 요청 DTO.
 * 초안 상태에서만 허용되며, 라인별 수량/단가를 수정할 수 있다.
 */
@Getter
@NoArgsConstructor
public class RevenueUpdateRequest {

	private Long employeeId;

	@NotNull
	private LocalDate revenueDate;

	private String remarks;

	@Valid
	@NotEmpty
	private List<LineItem> lines;

	@Getter
	@NoArgsConstructor
	public static class LineItem {

		@NotNull
		private Long id;

		@NotNull
		private BigDecimal quantity;

		@NotNull
		private BigDecimal unitPrice;

		private String remarks;
	}
}
