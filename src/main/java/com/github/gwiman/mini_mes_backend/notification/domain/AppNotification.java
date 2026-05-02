package com.github.gwiman.mini_mes_backend.notification.domain;

import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 엔티티.
 * <p>
 * SSE 연결이 없는 경우에도 DB에 영속되어 재연결 후 미수신 알림을 복구할 수 있다.
 * DB 영속화로 SSE 연결 유실 시에도 이력이 보존된다는 점이 Polling·WebSocket 대신 SSE를 채택한
 * 핵심 이유 중 하나다.
 * 테이블명을 {@code app_notification}으로 지정해 PostgreSQL 예약어 {@code notification} 충돌을 회피한다.
 * </p>
 *
 * @see <a href="../../../../../../../../doc/docs/adr/007-notification-sse-design.md">ADR-007: 알람 SSE + DB 테이블 방식 채택</a>
 */
@Entity
@Table(name = "app_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String recipientUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String message;

    /** 알림과 연관된 엔티티 PK (견적 ID, 발주 ID 등) — 프론트에서 해당 화면으로 이동할 때 사용 */
    private Long referenceId;

    @Column(nullable = false)
    private boolean isRead = false;

    private AppNotification(String recipientUsername, NotificationType type,
                            String message, Long referenceId) {
        this.recipientUsername = recipientUsername;
        this.type = type;
        this.message = message;
        this.referenceId = referenceId;
    }

    public static AppNotification create(String recipientUsername, NotificationType type,
                                         String message, Long referenceId) {
        return new AppNotification(recipientUsername, type, message, referenceId);
    }

    public void markRead() {
        this.isRead = true;
    }
}
