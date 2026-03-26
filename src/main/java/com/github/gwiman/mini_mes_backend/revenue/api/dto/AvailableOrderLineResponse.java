package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;

/**
 * 매출 생성 시 품목 선택 팝업용 DTO.
 * 거래처의 완료 수주에서 선택 가능한 수주 라인 정보를 담는다.
 */
public record AvailableOrderLineResponse(
	Long salesOrderLineId,
	Long salesOrderId,
	String orderNumber,
	Long itemId,
	String itemCode,
	String itemName,
	BigDecimal quantity,
	BigDecimal unitPrice
) {}
