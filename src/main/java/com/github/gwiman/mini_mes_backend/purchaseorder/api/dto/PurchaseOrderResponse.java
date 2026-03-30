package com.github.gwiman.mini_mes_backend.purchaseorder.api.dto;

import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
	Long id,
	String orderNumber,
	/** useCrudPage 호환용 — orderNumber와 동일 */
	String name,
	LocalDate orderDate,
	Long partnerId,
	String partnerName,
	LocalDate expectedArrivalDate,
	String statusCode,
	/** 구매 요청 전환 시 원본 PR ID — 직접 생성 시 null */
	Long prId,
	String remarks,
	List<PurchaseOrderLineResponse> lines
) {}
