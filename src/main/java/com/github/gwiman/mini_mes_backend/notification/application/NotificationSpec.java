package com.github.gwiman.mini_mes_backend.notification.application;

import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;

/**
 * 알림 생성에 필요한 명세 값 객체.
 * <p>
 * {@link NotificationFactory} 구현체가 이벤트로부터 생성하며,
 * {@link NotificationService}가 이를 받아 DB 저장 및 SSE 전송을 수행한다.
 * </p>
 *
 * @param recipientUsername 수신자 username
 * @param type              알림 유형 (메시지 템플릿 포함)
 * @param referenceId       연관 엔티티 PK — 프론트에서 해당 화면으로 이동할 때 사용
 * @param documentNumber    메시지 템플릿의 %s 자리에 채울 문서번호
 */
public record NotificationSpec(
        String recipientUsername,
        NotificationType type,
        Long referenceId,
        String documentNumber
) {}
