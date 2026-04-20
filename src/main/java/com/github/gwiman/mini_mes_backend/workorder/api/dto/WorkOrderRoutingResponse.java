package com.github.gwiman.mini_mes_backend.workorder.api.dto;

import org.jooq.Record;
import org.jooq.impl.DSL;

public record WorkOrderRoutingResponse(
		Long id,
		Long routingId,
		Long processId,
		String processCode,
		String processName,
		int stepOrder,
		Integer standardTime,
		String remarks
) {
	public static WorkOrderRoutingResponse fromRecord(Record r) {
		return new WorkOrderRoutingResponse(
			r.get(DSL.field("work_order_routing.id", Long.class)),
			r.get(DSL.field("work_order_routing.routing_id", Long.class)),
			r.get(DSL.field("work_order_routing.process_id", Long.class)),
			r.get(DSL.field("proc_code", String.class)),
			r.get(DSL.field("proc_name", String.class)),
			r.get(DSL.field("work_order_routing.step_order", Integer.class)),
			r.get(DSL.field("work_order_routing.standard_time", Integer.class)),
			r.get(DSL.field("work_order_routing.remarks", String.class))
		);
	}
}
