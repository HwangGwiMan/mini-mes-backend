package com.github.gwiman.mini_mes_backend.shipment.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출하 라인 응답 DTO.
 * 계획수량(수주 기반)과 실출하수량을 함께 반환하여 계획 대비 실적 비교를 지원한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentLineResponse {

	private Long id;
	private Long salesOrderLineId;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private BigDecimal plannedQuantity;
	private BigDecimal actualQuantity;
	private BigDecimal unitPrice;
	private BigDecimal plannedAmount;
	private BigDecimal actualAmount;
	private String remarks;
	private int sortOrder;
}
