package com.github.gwiman.mini_mes_backend.shipment.api.dto;

import java.math.BigDecimal;

/**
 * 출하 라인 응답 DTO.
 * 계획수량(수주 기반)과 실출하수량을 함께 반환하여 계획 대비 실적 비교를 지원한다.
 */
public record ShipmentLineResponse(
	Long id,
	Long salesOrderLineId,
	Long itemId,
	String itemCode,
	String itemName,
	BigDecimal plannedQuantity,
	BigDecimal actualQuantity,
	BigDecimal unitPrice,
	BigDecimal plannedAmount,
	BigDecimal actualAmount,
	String remarks,
	int sortOrder
) {}
