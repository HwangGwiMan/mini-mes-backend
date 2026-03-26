package com.github.gwiman.mini_mes_backend.itemprice.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemPriceRequest(
	@NotNull(message = "품목은 필수입니다.")
	Long itemId,

	@NotNull(message = "단가는 필수입니다.")
	@DecimalMin(value = "0", message = "단가는 0 이상이어야 합니다.")
	BigDecimal unitPrice,

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	String remarks
) {}
