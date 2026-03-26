package com.github.gwiman.mini_mes_backend.routing.api.dto;

import org.jooq.Record;

public record RoutingStepResponse(
	Long id,
	Long processId,
	String processCode,
	String processName,
	int stepOrder,
	Integer standardTime,
	String remarks
) {
	public static RoutingStepResponse fromRecord(Record r) {
		return new RoutingStepResponse(
			r.get("id", Long.class),
			r.get("process_id", Long.class),
			r.get("process_code", String.class),
			r.get("process_name", String.class),
			r.get("step_order", Integer.class),
			r.get("standard_time", Integer.class),
			r.get("remarks", String.class)
		);
	}
}
