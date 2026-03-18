package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매출 라인 응답 DTO.
 * salesOrderId/orderNumber는 원본 수주 추적 및 표시용이다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueLineResponse {

	private Long id;
	private Long salesOrderLineId;
	private Long salesOrderId;
	private String orderNumber;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal amount;
	private String remarks;
	private int sortOrder;
}
