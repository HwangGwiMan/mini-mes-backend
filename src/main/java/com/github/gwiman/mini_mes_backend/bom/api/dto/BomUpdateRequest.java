package com.github.gwiman.mini_mes_backend.bom.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record BomUpdateRequest(
	LocalDate validFrom,

	LocalDate validTo,

	@NotEmpty(message = "자재 라인은 최소 1개 이상이어야 합니다.")
	@Valid
	List<BomLineRequest> lines
) {}
