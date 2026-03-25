package com.github.gwiman.mini_mes_backend.bom.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BomCopyRequest {

	@NotBlank(message = "새 버전은 필수입니다.")
	private String newVersion;
}
