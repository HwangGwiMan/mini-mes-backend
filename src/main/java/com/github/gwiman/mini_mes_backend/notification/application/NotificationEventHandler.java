package com.github.gwiman.mini_mes_backend.notification.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.purchaseorder.application.GoodsReceiptConfirmedEvent;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseOrderCancelledEvent;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseOrderCreatedFromPREvent;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteApprovedEvent;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteRejectedEvent;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteSubmittedEvent;

import lombok.RequiredArgsConstructor;

/**
 * 알림 이벤트 핸들러 — 얇은 라우터.
 * <p>
 * {@code @ApplicationModuleListener} 메서드는 Spring Modulith 특성상 명시적 선언이 필수이므로
 * 각 이벤트를 {@link NotificationFactoryRegistry}에 위임해 처리 로직을 분리한다.
 * 새 알림 추가 시 Factory 클래스 1개 + 이 파일에 리스너 1줄만 추가하면 된다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationFactoryRegistry registry;
    private final NotificationService notificationService;

    @ApplicationModuleListener
    public void on(QuoteSubmittedEvent e)             { dispatch(e); }

    @ApplicationModuleListener
    public void on(QuoteApprovedEvent e)              { dispatch(e); }

    @ApplicationModuleListener
    public void on(QuoteRejectedEvent e)              { dispatch(e); }

    @ApplicationModuleListener
    public void on(PurchaseOrderCreatedFromPREvent e) { dispatch(e); }

    @ApplicationModuleListener
    public void on(PurchaseOrderCancelledEvent e)     { dispatch(e); }

    @ApplicationModuleListener
    public void on(GoodsReceiptConfirmedEvent e)      { dispatch(e); }

    private <E> void dispatch(E event) {
        registry.create(event).ifPresent(notificationService::createAndSend);
    }
}
