package com.github.gwiman.mini_mes_backend.price.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemPriceRequest {

	@NotNull(message = "품목은 필수입니다.")
	private Long itemId;

	@NotNull(message = "단가는 필수입니다.")
	@DecimalMin(value = "0", message = "단가는 0 이상이어야 합니다.")
	private BigDecimal unitPrice;

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	private String remarks;
}
