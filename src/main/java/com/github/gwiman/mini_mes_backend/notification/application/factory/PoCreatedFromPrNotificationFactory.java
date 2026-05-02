package com.github.gwiman.mini_mes_backend.notification.application.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.notification.application.NotificationFactory;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationSpec;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseOrderCreatedFromPREvent;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseRequestService;

import lombok.RequiredArgsConstructor;

/**
 * 구매 요청 → 발주 전환 알림 팩토리. 수신자는 구매 요청 생성자.
 */
@Component
@RequiredArgsConstructor
public class PoCreatedFromPrNotificationFactory
        implements NotificationFactory<PurchaseOrderCreatedFromPREvent> {

    private final PurchaseRequestService purchaseRequestService;

    @Override
    public Class<PurchaseOrderCreatedFromPREvent> eventType() {
        return PurchaseOrderCreatedFromPREvent.class;
    }

    @Override
    public Optional<NotificationSpec> from(PurchaseOrderCreatedFromPREvent event) {
        if (event.prId() == null) return Optional.empty();
        String requesterUsername = purchaseRequestService.findCreatedByById(event.prId());
        if (requesterUsername == null) return Optional.empty();
        return Optional.of(new NotificationSpec(
            requesterUsername,
            NotificationType.PO_CREATED_FROM_PR,
            event.purchaseOrderId(),
            event.poNumber()
        ));
    }
}
