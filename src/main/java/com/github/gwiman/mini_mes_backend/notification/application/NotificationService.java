package com.github.gwiman.mini_mes_backend.notification.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.notification.api.dto.NotificationDto;
import com.github.gwiman.mini_mes_backend.notification.domain.AppNotification;
import com.github.gwiman.mini_mes_backend.notification.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;

/**
 * 알림 애플리케이션 서비스.
 * <p>
 * {@link NotificationSpec}을 받아 메시지를 조립하고 DB에 저장한 뒤 SSE로 실시간 전송한다.
 * SSE 전송 실패(연결 없음)는 정상 상황 — DB에 저장됐으므로 재연결 후 REST API로 복구 가능.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * 알림을 생성하고 대상 사용자에게 실시간으로 전송한다.
     * {@code spec.type().formatMessage(spec.documentNumber())}로 메시지를 조립한다.
     */
    @Transactional
    public void createAndSend(NotificationSpec spec) {
        if (spec.recipientUsername() == null) return;

        String message = spec.type().formatMessage(spec.documentNumber());
        AppNotification notification = AppNotification.create(
            spec.recipientUsername(), spec.type(), message, spec.referenceId()
        );
        notificationRepository.save(notification);

        sseEmitterRegistry.send(spec.recipientUsername(), NotificationDto.from(notification));
    }

    /** 최근 50건 알림 조회 */
    public List<NotificationDto> getNotifications(String username) {
        return notificationRepository
            .findTop50ByRecipientUsernameOrderByCreatedAtDesc(username)
            .stream()
            .map(NotificationDto::from)
            .toList();
    }

    /** 단건 읽음 처리 */
    @Transactional
    public void markRead(Long id, String username) {
        AppNotification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("알림을 찾을 수 없습니다: " + id));
        if (!notification.getRecipientUsername().equals(username)) {
            throw new ResourceNotFoundException("알림을 찾을 수 없습니다: " + id);
        }
        notification.markRead();
    }

    /** 전체 읽음 처리 */
    @Transactional
    public void markAllRead(String username) {
        notificationRepository.markAllReadByUsername(username);
    }
}
