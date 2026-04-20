package com.github.gwiman.mini_mes_backend.workorder.api.dto;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.impl.DSL;

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
	public static WorkOrderMaterialResponse fromRecord(Record r) {
		return new WorkOrderMaterialResponse(
			r.get(DSL.field("work_order_material.id", Long.class)),
			r.get(DSL.field("work_order_material.material_item_id", Long.class)),
			r.get(DSL.field("mat_item_code", String.class)),
			r.get(DSL.field("mat_item_name", String.class)),
			r.get(DSL.field("work_order_material.warehouse_id", Long.class)),
			r.get(DSL.field("mat_wh_name", String.class)),
			r.get(DSL.field("work_order_material.planned_qty", BigDecimal.class)),
			r.get(DSL.field("work_order_material.sort_order", Integer.class))
		);
	}
}
