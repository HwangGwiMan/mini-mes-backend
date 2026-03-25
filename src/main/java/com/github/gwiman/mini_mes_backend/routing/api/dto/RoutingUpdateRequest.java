package com.github.gwiman.mini_mes_backend.routing.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoutingUpdateRequest {

	@NotEmpty
	@Valid
	private List<RoutingStepRequest> steps;
}
