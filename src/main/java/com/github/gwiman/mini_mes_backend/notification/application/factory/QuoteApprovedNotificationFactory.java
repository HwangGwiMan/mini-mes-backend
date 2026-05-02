package com.github.gwiman.mini_mes_backend.notification.application.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.notification.application.NotificationFactory;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationSpec;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteApprovedEvent;

/**
 * 견적 승인 알림 팩토리. 수신자는 견적 등록자(quoterUsername).
 */
@Component
public class QuoteApprovedNotificationFactory implements NotificationFactory<QuoteApprovedEvent> {

    @Override
    public Class<QuoteApprovedEvent> eventType() {
        return QuoteApprovedEvent.class;
    }

    @Override
    public Optional<NotificationSpec> from(QuoteApprovedEvent event) {
        if (event.quoterUsername() == null) return Optional.empty();
        return Optional.of(new NotificationSpec(
            event.quoterUsername(),
            NotificationType.QUOTE_APPROVED,
            event.quoteId(),
            event.quoteNumber()
        ));
    }
}
