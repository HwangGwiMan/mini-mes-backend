package com.github.gwiman.mini_mes_backend.bom.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BomCreateRequest {

	@NotNull(message = "완제품 품목 ID는 필수입니다.")
	private Long itemId;

	@NotBlank(message = "버전은 필수입니다.")
	private String version;

	private LocalDate validFrom;

	private LocalDate validTo;

	@NotEmpty(message = "자재 라인은 최소 1개 이상이어야 합니다.")
	@Valid
	private List<BomLineRequest> lines;
}
