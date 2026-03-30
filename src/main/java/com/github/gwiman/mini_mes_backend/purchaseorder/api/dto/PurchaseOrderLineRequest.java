package com.github.gwiman.mini_mes_backend.purchaseorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchaseOrderLineRequest(
	@NotNull Long itemId,
	@NotNull @DecimalMin("0.0001") BigDecimal orderedQuantity,
	BigDecimal unitPrice,
	LocalDate requiredDate,
	@Size(max = 200) String remarks,
	int sortOrder,
	/** 구매 요청 전환 시 원본 PR 라인 ID — 직접 생성 시 null */
	Long prLineId
) {}
