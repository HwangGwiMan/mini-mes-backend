package com.github.gwiman.mini_mes_backend.notification.api.dto;

import java.time.LocalDateTime;

import com.github.gwiman.mini_mes_backend.notification.domain.AppNotification;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationType;

/**
 * 알림 응답 DTO.
 *
 * @param id          알림 PK
 * @param type        알림 유형
 * @param message     조립된 최종 메시지
 * @param referenceId 연관 엔티티 PK (프론트에서 해당 화면으로 이동할 때 사용)
 * @param isRead      읽음 여부
 * @param createdAt   생성일시
 */
public record NotificationDto(
        Long id,
        NotificationType type,
        String message,
        Long referenceId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationDto from(AppNotification n) {
        return new NotificationDto(
            n.getId(),
            n.getType(),
            n.getMessage(),
            n.getReferenceId(),
            n.isRead(),
            n.getCreatedAt()
        );
    }
}
