package com.github.gwiman.mini_mes_backend.bom.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BomLineRequest {

	@NotNull(message = "자재 품목 ID는 필수입니다.")
	private Long materialItemId;

	@NotNull(message = "소요량은 필수입니다.")
	@Positive(message = "소요량은 0보다 커야 합니다.")
	private BigDecimal quantity;

	private String unit;

	private String remarks;
}
