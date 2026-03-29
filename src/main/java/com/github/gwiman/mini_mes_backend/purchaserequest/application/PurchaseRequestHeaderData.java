package com.github.gwiman.mini_mes_backend.purchaserequest.application;

/**
 * 구매 요청 헤더 내부 전달 객체.
 * 타 모듈(purchaseorder)에서 구매 요청 전환 시 필요한 최소 정보만 노출한다 — api DTO 직접 노출 방지.
 */
public record PurchaseRequestHeaderData(
	Long id,
	String requestNumber,
	String statusCode,
	Long requesterId
) {}
