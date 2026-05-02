package com.github.gwiman.mini_mes_backend.notification.application;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결을 username 기준으로 관리하는 레지스트리.
 * <p>
 * 동일 username이 재연결하면 기존 emitter를 교체한다.
 * {@code emitters.remove(username, emitter)} value 비교 삭제로 재연결 직후 잘못된 제거를 방지한다.
 * 단일 인스턴스 운영 기준 설계 — 스케일아웃 시 SseEmitter가 인스턴스 로컬이므로
 * sticky session 또는 별도 pub/sub 계층이 필요하다(ADR-007 트레이드오프 참고).
 * </p>
 *
 * @see <a href="../../../../../../../../doc/docs/adr/007-notification-sse-design.md">ADR-007: 알람 SSE + DB 테이블 방식 채택</a>
 */
@Component
public class SseEmitterRegistry {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L; // 30분

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * username에 대한 새 SSE emitter를 등록하고 반환한다.
     * 기존 연결이 있으면 complete 처리 후 교체한다.
     */
    public SseEmitter register(String username) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        SseEmitter old = emitters.put(username, emitter);
        if (old != null) {
            old.complete();
        }

        Runnable cleanup = () -> emitters.remove(username, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // 연결 직후 handshake 이벤트 — 일부 브라우저의 초기 연결 확인용
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("connected"));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    /**
     * 해당 username의 SSE 연결로 데이터를 전송한다.
     * 연결이 없거나 전송 실패 시 조용히 무시한다 — DB에 이미 저장됐으므로 재연결 후 복구 가능.
     */
    public void send(String username, Object data) {
        SseEmitter emitter = emitters.get(username);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name("NOTIFICATION").data(data));
        } catch (IOException e) {
            emitters.remove(username, emitter);
        }
    }
}
