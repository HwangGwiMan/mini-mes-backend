package com.github.gwiman.mini_mes_backend.notification.application.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.notification.application.NotificationFactory;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationSpec;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteRejectedEvent;

/**
 * 견적 반려 알림 팩토리. 수신자는 견적 등록자(quoterUsername).
 */
@Component
public class QuoteRejectedNotificationFactory implements NotificationFactory<QuoteRejectedEvent> {

    @Override
    public Class<QuoteRejectedEvent> eventType() {
        return QuoteRejectedEvent.class;
    }

    @Override
    public Optional<NotificationSpec> from(QuoteRejectedEvent event) {
        if (event.quoterUsername() == null) return Optional.empty();
        return Optional.of(new NotificationSpec(
            event.quoterUsername(),
            NotificationType.QUOTE_REJECTED,
            event.quoteId(),
            event.quoteNumber()
        ));
    }
}
