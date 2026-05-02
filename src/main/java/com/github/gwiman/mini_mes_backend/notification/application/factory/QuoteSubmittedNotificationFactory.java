package com.github.gwiman.mini_mes_backend.notification.application.factory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.auth.application.AuthService;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationFactory;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationSpec;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteSubmittedEvent;

import lombok.RequiredArgsConstructor;

/**
 * 견적 승인 요청 제출 알림 팩토리.
 * approverId(사원 PK) → username 변환 후 승인권자에게 알림을 생성한다.
 */
@Component
@RequiredArgsConstructor
public class QuoteSubmittedNotificationFactory implements NotificationFactory<QuoteSubmittedEvent> {

    private final AuthService authService;

    @Override
    public Class<QuoteSubmittedEvent> eventType() {
        return QuoteSubmittedEvent.class;
    }

    @Override
    public Optional<NotificationSpec> from(QuoteSubmittedEvent event) {
        if (event.approverId() == null) return Optional.empty();
        String approverUsername = authService.findUsernameByEmployeeId(event.approverId());
        if (approverUsername == null) return Optional.empty();
        return Optional.of(new NotificationSpec(
            approverUsername,
            NotificationType.QUOTE_SUBMITTED,
            event.quoteId(),
            event.quoteNumber()
        ));
    }
}
