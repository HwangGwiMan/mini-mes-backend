package com.github.gwiman.mini_mes_backend.notification.application.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.notification.application.NotificationFactory;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationSpec;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;
import com.github.gwiman.mini_mes_backend.purchaseorder.application.GoodsReceiptConfirmedEvent;
import com.github.gwiman.mini_mes_backend.purchaseorder.application.PurchaseOrderService;

import lombok.RequiredArgsConstructor;

/**
 * 자재 입고 확정 알림 팩토리. 수신자는 구매 발주 생성자(발주 담당자).
 */
@Component
@RequiredArgsConstructor
public class GoodsReceiptConfirmedNotificationFactory
        implements NotificationFactory<GoodsReceiptConfirmedEvent> {

    private final PurchaseOrderService purchaseOrderService;

    @Override
    public Class<GoodsReceiptConfirmedEvent> eventType() {
        return GoodsReceiptConfirmedEvent.class;
    }

    @Override
    public Optional<NotificationSpec> from(GoodsReceiptConfirmedEvent event) {
        if (event.poId() == null) return Optional.empty();
        String poCreatedBy = purchaseOrderService.findCreatedByById(event.poId());
        if (poCreatedBy == null) return Optional.empty();
        return Optional.of(new NotificationSpec(
            poCreatedBy,
            NotificationType.GOODS_RECEIPT_CONFIRMED,
            event.goodsReceiptId(),
            event.receiptNumber()
        ));
    }
}
