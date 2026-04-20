package com.github.gwiman.mini_mes_backend.workorder.api.dto;

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
}
