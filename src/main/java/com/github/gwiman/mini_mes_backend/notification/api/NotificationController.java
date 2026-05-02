package com.github.gwiman.mini_mes_backend.notification.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.github.gwiman.mini_mes_backend.notification.api.dto.NotificationDto;
import com.github.gwiman.mini_mes_backend.notification.application.NotificationService;
import com.github.gwiman.mini_mes_backend.notification.application.SseEmitterRegistry;

import lombok.RequiredArgsConstructor;

/**
 * 알림 API 컨트롤러.
 * <p>
 * SSE 스트림({@code /subscribe})과 REST(조회/읽음 처리) 엔드포인트를 제공한다.
 * SSE 연결은 JWT 토큰을 쿼리파라미터로 전달받아 인증한다.
 * SSE 재연결 후 미수신 알림은 {@code GET /api/notifications}로 복구한다.
 * </p>
 *
 * @see <a href="../../../../../../../../../../../doc/docs/adr/007-notification-sse-design.md">ADR-007: 알람 SSE + DB 테이블 방식 채택</a>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final NotificationService notificationService;

    /**
     * SSE 연결 엔드포인트.
     * EventSource가 Authorization 헤더를 지원하지 않으므로 쿼리파라미터 token으로 인증한다.
     * JwtAuthenticationFilter가 토큰을 검증하고 SecurityContext에 인증 정보를 설정한다.
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return sseEmitterRegistry.register(userDetails.getUsername());
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getNotifications(userDetails.getUsername()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markRead(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
