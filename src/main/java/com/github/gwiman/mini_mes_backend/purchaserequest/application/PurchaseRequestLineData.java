package com.github.gwiman.mini_mes_backend.purchaserequest.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 구매 요청 라인 내부 전달 객체.
 * 구매 발주 전환 시 라인 정보를 전달하는 데 사용된다.
 */
public record PurchaseRequestLineData(
	Long lineId,
	Long itemId,
	BigDecimal requestedQuantity,
	LocalDate requiredDate,
	String remarks,
	int sortOrder
) {}
