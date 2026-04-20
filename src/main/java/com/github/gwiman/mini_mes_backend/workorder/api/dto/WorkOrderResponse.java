package com.github.gwiman.mini_mes_backend.workorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WorkOrderResponse(
		Long id,
		String workOrderNumber,
		/** useCrudPage 호환용 — workOrderNumber와 동일 */
		String name,
		Long salesOrderId,
		String salesOrderNumber,
		Long salesOrderLineId,
		Long itemId,
		String itemCode,
		String itemName,
		Long bomId,
		String bomVersionCode,
		Long warehouseId,
		String warehouseName,
		BigDecimal plannedQty,
		String statusCode,
		LocalDate plannedStartDate,
		LocalDate plannedEndDate,
		String remarks,
		List<WorkOrderMaterialResponse> materials,
		List<WorkOrderRoutingResponse> routings
) {
}
