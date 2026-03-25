package com.github.gwiman.mini_mes_backend.routing.api.dto;

import org.jooq.Record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingStepResponse {

	private Long id;
	private Long processId;
	private String processCode;
	private String processName;
	private int stepOrder;
	private Integer standardTime;
	private String remarks;

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
