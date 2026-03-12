package com.github.gwiman.mini_mes_backend.common.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 모든 엔티티에 공통 적용되는 Audit 베이스 클래스.
 * <p>
 * JPA Auditing을 통해 생성/수정 일시 및 사용자를 자동 기록한다.
 * {@code AuditorAware} 구현체({@code JpaAuditingConfig})가 현재 로그인 사용자를 제공한다.
 * </p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false, length = 50)
    private String createdBy;

    @LastModifiedBy
    @Column(length = 50)
    private String updatedBy;
}
