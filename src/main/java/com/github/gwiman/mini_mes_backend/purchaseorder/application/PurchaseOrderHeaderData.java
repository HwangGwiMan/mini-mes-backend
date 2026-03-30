package com.github.gwiman.mini_mes_backend.purchaseorder.application;

/**
 * 구매 발주 헤더 내부 전달 객체.
 * Phase 3 자재입고(PurchaseReceipt) 도메인에서 발주 정보를 참조할 때 사용한다 — api DTO 직접 노출 방지.
 */
public record PurchaseOrderHeaderData(
	Long id,
	String orderNumber,
	String statusCode,
	Long partnerId
) {}
