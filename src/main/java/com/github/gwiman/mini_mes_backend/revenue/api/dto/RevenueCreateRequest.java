package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 매출 생성 요청 DTO.
 * 거래처를 지정하고 해당 거래처의 완료 수주 라인 중 선택한 항목으로 매출을 구성한다.
 */
public record RevenueCreateRequest(
	@NotNull
	Long partnerId,

	Long employeeId,

	@NotNull
	LocalDate revenueDate,

	String remarks,

	@Valid
	@NotEmpty
	List<LineItem> lines
) {
	public record LineItem(
		@NotNull Long salesOrderLineId,
		@NotNull Long salesOrderId,
		@NotNull Long itemId,
		@NotNull BigDecimal quantity,
		@NotNull BigDecimal unitPrice,
		String remarks
	) {}
}
