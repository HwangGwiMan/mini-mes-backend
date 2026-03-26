package com.github.gwiman.mini_mes_backend.routing.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record RoutingUpdateRequest(
	@NotEmpty
	@Valid
	List<RoutingStepRequest> steps
) {}
