package com.github.gwiman.mini_mes_backend.notification.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 모든 {@link NotificationFactory} 빈을 자동 수집해 이벤트 타입으로 조회하는 레지스트리.
 * <p>
 * Spring이 {@code List<NotificationFactory<?>>}를 통해 모든 구현체를 주입한다.
 * 새 알림 추가 시 {@code @Component} Factory 클래스만 추가하면 자동으로 등록된다.
 * </p>
 */
@Component
public class NotificationFactoryRegistry {

    private final Map<Class<?>, NotificationFactory<?>> factories;

    public NotificationFactoryRegistry(List<NotificationFactory<?>> factories) {
        this.factories = factories.stream()
            .collect(Collectors.toMap(NotificationFactory::eventType, Function.identity()));
    }

    /**
     * 이벤트에 해당하는 Factory를 찾아 {@link NotificationSpec}을 생성한다.
     * Factory가 없거나 {@code Optional.empty()}를 반환하면 빈 Optional을 반환한다.
     */
    @SuppressWarnings("unchecked")
    public <E> Optional<NotificationSpec> create(E event) {
        NotificationFactory<E> factory = (NotificationFactory<E>) factories.get(event.getClass());
        if (factory == null) return Optional.empty();
        return factory.from(event);
    }
}
