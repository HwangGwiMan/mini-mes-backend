package com.github.gwiman.mini_mes_backend.materialissue.api.dto;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.impl.DSL;

public record MaterialIssueLineResponse(
		Long id,
		Long workOrderMaterialId,
		Long materialItemId,
		String materialItemCode,
		String materialItemName,
		Long warehouseId,
		String warehouseName,
		String lotNo,
		BigDecimal issuedQty,
		int sortOrder
) {
	public static MaterialIssueLineResponse fromRecord(Record r) {
		return new MaterialIssueLineResponse(
				r.get(DSL.field("material_issue_line.id", Long.class)),
				r.get(DSL.field("material_issue_line.work_order_material_id", Long.class)),
				r.get(DSL.field("material_issue_line.material_item_id", Long.class)),
				r.get(DSL.field("item_code", String.class)),
				r.get(DSL.field("item_name", String.class)),
				r.get(DSL.field("material_issue_line.warehouse_id", Long.class)),
				r.get(DSL.field("warehouse_name", String.class)),
				r.get(DSL.field("material_issue_line.lot_no", String.class)),
				r.get(DSL.field("material_issue_line.issued_qty", BigDecimal.class)),
				r.get(DSL.field("material_issue_line.sort_order", Integer.class))
		);
	}
}
