package com.github.gwiman.mini_mes_backend.purchaserequest.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequestLineResponse(
	Long id,
	Long itemId,
	String itemCode,
	String itemName,
	BigDecimal requestedQuantity,
	LocalDate requiredDate,
	String remarks,
	int sortOrder
) {}
