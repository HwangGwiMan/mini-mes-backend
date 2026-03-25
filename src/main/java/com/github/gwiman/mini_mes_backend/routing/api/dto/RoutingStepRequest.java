package com.github.gwiman.mini_mes_backend.routing.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoutingStepRequest {

	@NotNull
	private Long processId;

	@NotNull
	private Integer stepOrder;

	private Integer standardTime;

	private String remarks;
}
