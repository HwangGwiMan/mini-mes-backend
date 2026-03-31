package com.github.gwiman.mini_mes_backend.purchaserequest.application;

/**
 * 구매 발주(PO)가 취소될 때 발행되는 이벤트.
 * purchaseorder 모듈이 publish하고, purchaserequest 모듈의 PurchaseRequestEventHandler가 subscribe해 PR 상태를 승인됨으로 복원한다.
 * prId가 null이면 PR과 무관한 직접 생성 발주이므로 핸들러에서 무시된다.
 */
public record PurchaseOrderCancelledEvent(Long purchaseOrderId, Long prId) {}
