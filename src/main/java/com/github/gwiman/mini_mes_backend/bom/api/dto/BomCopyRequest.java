package com.github.gwiman.mini_mes_backend.bom.api.dto;

import jakarta.validation.constraints.NotBlank;

public record BomCopyRequest(
	@NotBlank(message = "새 버전은 필수입니다.")
	String newVersion
) {}
