package com.github.gwiman.mini_mes_backend.quote.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuoteLineRequest(
	@NotNull(message = "품목은 필수입니다.")
	Long itemId,

	@NotNull(message = "수량은 필수입니다.")
	@DecimalMin(value = "0.0001", message = "수량은 0보다 커야 합니다.")
	BigDecimal quantity,

	@NotNull(message = "단가는 필수입니다.")
	@DecimalMin(value = "0", message = "단가는 0 이상이어야 합니다.")
	BigDecimal unitPrice,

	LocalDate deliveryRequestDate,

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	String remarks,

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	int sortOrder
) {}
