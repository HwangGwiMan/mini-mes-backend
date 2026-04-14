package com.github.gwiman.mini_mes_backend.workorder.api.dto;

import java.math.BigDecimal;

public record WorkOrderMaterialResponse(
		Long id,
		Long materialItemId,
		String materialItemCode,
		String materialItemName,
		Long warehouseId,
		String warehouseName,
		BigDecimal plannedQty,
		int sortOrder
) {
}
