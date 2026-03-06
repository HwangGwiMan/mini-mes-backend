package com.github.gwiman.mini_mes_backend.quote.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteLineData(
	Long itemId,
	BigDecimal quantity,
	BigDecimal unitPrice,
	LocalDate deliveryRequestDate,
	String remarks,
	int sortOrder
) {}
