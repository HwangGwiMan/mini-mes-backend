package com.github.gwiman.mini_mes_backend.workorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkOrderRequest(
		/** 연결 수주 ID — 독립 생성 시 null 허용 */
		Long salesOrderId,
		/** 연결 수주 라인 ID — 독립 생성 시 null 허용 */
		Long salesOrderLineId,
		@NotNull Long itemId,
		@NotNull Long bomId,
		@NotNull Long warehouseId,
		@NotNull @Positive BigDecimal plannedQty,
		@NotNull LocalDate plannedStartDate,
		LocalDate plannedEndDate,
		String remarks
) {
}
