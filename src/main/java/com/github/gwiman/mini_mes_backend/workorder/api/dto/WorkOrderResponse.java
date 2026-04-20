package com.github.gwiman.mini_mes_backend.workorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.jooq.Record;
import org.jooq.impl.DSL;

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
	/** search 쿼리 결과 레코드로부터 헤더 정보를 매핑한다. materials/routings는 빈 리스트로 초기화. */
	public static WorkOrderResponse fromRecord(Record r) {
		String workOrderNumber = r.get(DSL.field("work_order.work_order_number", String.class));
		return new WorkOrderResponse(
			r.get(DSL.field("work_order.id", Long.class)),
			workOrderNumber,
			workOrderNumber,
			r.get(DSL.field("work_order.sales_order_id", Long.class)),
			r.get(DSL.field("sales_order_number", String.class)),
			r.get(DSL.field("work_order.sales_order_line_id", Long.class)),
			r.get(DSL.field("work_order.item_id", Long.class)),
			r.get(DSL.field("item_code", String.class)),
			r.get(DSL.field("item_name", String.class)),
			r.get(DSL.field("work_order.bom_id", Long.class)),
			r.get(DSL.field("bom_version_code", String.class)),
			r.get(DSL.field("work_order.warehouse_id", Long.class)),
			r.get(DSL.field("warehouse_name", String.class)),
			r.get(DSL.field("work_order.planned_qty", BigDecimal.class)),
			r.get(DSL.field("work_order.status_code", String.class)),
			r.get(DSL.field("work_order.planned_start_date", LocalDate.class)),
			r.get(DSL.field("work_order.planned_end_date", LocalDate.class)),
			r.get(DSL.field("work_order.remarks", String.class)),
			List.of(),
			List.of()
		);
	}
}
