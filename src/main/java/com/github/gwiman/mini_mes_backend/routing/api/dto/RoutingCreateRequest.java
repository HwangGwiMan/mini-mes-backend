package com.github.gwiman.mini_mes_backend.routing.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RoutingCreateRequest(
	@NotNull
	Long bomId,

	@NotEmpty
	@Valid
	List<RoutingStepRequest> steps
) {}
