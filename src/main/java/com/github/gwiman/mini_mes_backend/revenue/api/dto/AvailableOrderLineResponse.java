package com.github.gwiman.mini_mes_backend.revenue.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매출 생성 시 품목 선택 팝업용 DTO.
 * 거래처의 완료 수주에서 선택 가능한 수주 라인 정보를 담는다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableOrderLineResponse {

	private Long salesOrderLineId;
	private Long salesOrderId;
	private String orderNumber;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
}
