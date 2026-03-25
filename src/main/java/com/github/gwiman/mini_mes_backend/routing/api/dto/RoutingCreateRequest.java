package com.github.gwiman.mini_mes_backend.routing.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoutingCreateRequest {

	@NotNull
	private Long bomId;

	@NotEmpty
	@Valid
	private List<RoutingStepRequest> steps;
}
