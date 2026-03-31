package com.github.gwiman.mini_mes_backend.purchaserequest.application;

/**
 * 구매 요청(PR)으로부터 구매 발주(PO)가 생성될 때 발행되는 이벤트.
 * purchaseorder 모듈이 publish하고, purchaserequest 모듈의 PurchaseRequestEventHandler가 subscribe해 PR 상태를 발주됨으로 전이한다.
 * 이벤트를 purchaserequest 모듈에 정의한 이유: purchaseorder 모듈에 두면 purchaserequest → purchaseorder 역방향 의존이 생기기 때문이다.
 */
public record PurchaseOrderCreatedFromPREvent(Long purchaseOrderId, Long prId) {}
