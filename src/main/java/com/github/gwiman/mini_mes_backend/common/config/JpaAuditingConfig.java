package com.github.gwiman.mini_mes_backend.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JPA Auditing 설정.
 * <p>
 * {@code AuditorAware} 빈이 {@code SecurityContextHolder}에서 현재 사용자 이름을 읽어
 * {@code @CreatedBy} / {@code @LastModifiedBy} 필드에 자동 주입한다.
 * SecurityContext가 비어 있는 경우(배치, 초기 데이터 로드 등)는 "system"으로 기록한다.
 * </p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
