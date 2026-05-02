package com.github.gwiman.mini_mes_backend.notification.application;

import java.util.Optional;

/**
 * 도메인 이벤트를 {@link NotificationSpec}으로 변환하는 팩토리 인터페이스.
 * <p>
 * 새 알림 유형 추가 시 이 인터페이스를 구현하는 클래스를 {@code factory/} 패키지에 추가하고
 * {@code @Component}를 붙이면 {@link NotificationFactoryRegistry}가 자동 수집한다.
 * 알림이 불필요한 경우(예: PR 없는 발주 취소) {@code Optional.empty()}를 반환한다.
 * </p>
 *
 * @param <E> 수신할 이벤트 타입
 */
public interface NotificationFactory<E> {

    /** 이 팩토리가 처리하는 이벤트 클래스 — Registry가 타입 매핑에 사용한다. */
    Class<E> eventType();

    /**
     * 이벤트를 알림 명세로 변환한다.
     *
     * @param event 수신된 도메인 이벤트
     * @return 알림 명세, 알림 불필요 시 {@code Optional.empty()}
     */
    Optional<NotificationSpec> from(E event);
}
