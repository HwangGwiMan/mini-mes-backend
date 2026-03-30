package com.github.gwiman.mini_mes_backend.purchaseorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseOrderLineResponse(
	Long id,
	Long itemId,
	String itemCode,
	String itemName,
	BigDecimal orderedQuantity,
	BigDecimal unitPrice,
	LocalDate requiredDate,
	String remarks,
	int sortOrder,
	/** 구매 요청 전환 시 원본 PR 라인 ID — 직접 생성 시 null */
	Long prLineId
) {}
