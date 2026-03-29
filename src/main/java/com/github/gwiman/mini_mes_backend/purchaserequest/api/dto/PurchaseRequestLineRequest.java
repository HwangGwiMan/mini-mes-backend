package com.github.gwiman.mini_mes_backend.purchaserequest.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchaseRequestLineRequest(
	@NotNull(message = "품목은 필수입니다.")
	Long itemId,

	@NotNull(message = "요청 수량은 필수입니다.")
	@DecimalMin(value = "0.0001", message = "요청 수량은 0보다 커야 합니다.")
	BigDecimal requestedQuantity,

	LocalDate requiredDate,

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	String remarks,

	int sortOrder
) {}
