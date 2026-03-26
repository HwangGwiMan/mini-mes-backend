package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;

/**
 * 매출 라인 응답 DTO.
 * salesOrderId/orderNumber는 원본 수주 추적 및 표시용이다.
 */
public record RevenueLineResponse(
	Long id,
	Long salesOrderLineId,
	Long salesOrderId,
	String orderNumber,
	Long itemId,
	String itemCode,
	String itemName,
	BigDecimal quantity,
	BigDecimal unitPrice,
	BigDecimal amount,
	String remarks,
	int sortOrder
) {}
