package com.github.gwiman.mini_mes_backend.routing.api.dto;

import jakarta.validation.constraints.NotNull;

public record RoutingStepRequest(
	@NotNull
	Long processId,

	@NotNull
	Integer stepOrder,

	Integer standardTime,

	String remarks
) {}
