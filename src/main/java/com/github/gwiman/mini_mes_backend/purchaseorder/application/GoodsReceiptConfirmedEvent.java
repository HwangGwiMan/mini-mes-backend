package com.github.gwiman.mini_mes_backend.purchaseorder.application;

/**
 * 자재 입고가 확정될 때 발행되는 이벤트.
 * goodsreceipt 모듈이 publish하고, purchaseorder 모듈의 PurchaseOrderEventHandler가 subscribe해 PO를 입고완료로 전이한다.
 * 이벤트를 purchaseorder 모듈에 정의한 이유: goodsreceipt 모듈에 두면 purchaseorder → goodsreceipt 역방향 의존이 생기기 때문이다.
 */
public record GoodsReceiptConfirmedEvent(Long goodsReceiptId, Long poId, String receiptNumber) {}
