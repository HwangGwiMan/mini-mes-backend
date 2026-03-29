package com.github.gwiman.mini_mes_backend.orderfulfillment.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 수주이행현황 조회 응답 DTO.
 * 수주 헤더 + 연결된 출하 집계 + 마감 매출 집계를 단일 레코드로 반환한다.
 * 이행률(fulfillmentRate)은 실제 출하금액 / 수주금액 × 100으로 계산된다.
 */
public record OrderFulfillmentResponse(
	Long salesOrderId,
	String orderNumber,
	LocalDate orderDate,
	LocalDate deliveryDate,
	String partnerName,
	String employeeName,
	String orderStatusCode,

	// 출하 정보 (수주 1건 = 출하 1건, 없을 수 있음)
	Long shipmentId,
	String shipmentNumber,
	String shipmentStatusCode,
	LocalDate shipmentDate,
	BigDecimal totalPlannedAmount,
	BigDecimal totalActualAmount,

	// 수주 총금액
	BigDecimal totalOrderAmount,

	// 마감(REVENUE_STATUS_02) 기준 매출 합계
	BigDecimal totalRevenueAmount,

	// "없음" | "초안" | "마감" | "혼재" — 연결된 매출 상태 요약
	String revenueStatusSummary,

	// 실제 출하금액 / 수주금액 × 100, 출하 완료 전이면 null
	BigDecimal fulfillmentRate
) {}
