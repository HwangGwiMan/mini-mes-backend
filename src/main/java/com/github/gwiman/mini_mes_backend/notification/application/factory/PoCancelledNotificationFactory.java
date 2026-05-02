package com.github.gwiman.mini_mes_backend.notification.application.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.notification.application.NotificationFactory;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationSpec;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseOrderCancelledEvent;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseRequestService;

import lombok.RequiredArgsConstructor;

/**
 * 발주 취소 알림 팩토리.
 * prId가 null(직접 생성 발주)이면 알림 불필요 — Optional.empty() 반환.
 */
@Component
@RequiredArgsConstructor
public class PoCancelledNotificationFactory
        implements NotificationFactory<PurchaseOrderCancelledEvent> {

    private final PurchaseRequestService purchaseRequestService;

    @Override
    public Class<PurchaseOrderCancelledEvent> eventType() {
        return PurchaseOrderCancelledEvent.class;
    }

    @Override
    public Optional<NotificationSpec> from(PurchaseOrderCancelledEvent event) {
        // PR 없는 직접 생성 발주 취소는 알림 불필요
        if (event.prId() == null) return Optional.empty();
        String requesterUsername = purchaseRequestService.findCreatedByById(event.prId());
        if (requesterUsername == null) return Optional.empty();
        return Optional.of(new NotificationSpec(
            requesterUsername,
            NotificationType.PO_CANCELLED,
            event.purchaseOrderId(),
            event.poNumber()
        ));
    }
}
