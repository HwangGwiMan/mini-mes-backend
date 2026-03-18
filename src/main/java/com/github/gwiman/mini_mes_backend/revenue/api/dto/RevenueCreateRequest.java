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
 * 매출 생성 요청 DTO.
 * 거래처를 지정하고 해당 거래처의 완료 수주 라인 중 선택한 항목으로 매출을 구성한다.
 */
@Getter
@NoArgsConstructor
public class RevenueCreateRequest {

	@NotNull
	private Long partnerId;

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
		private Long salesOrderLineId;

		@NotNull
		private Long salesOrderId;

		@NotNull
		private Long itemId;

		@NotNull
		private BigDecimal quantity;

		@NotNull
		private BigDecimal unitPrice;

		private String remarks;
	}
}
