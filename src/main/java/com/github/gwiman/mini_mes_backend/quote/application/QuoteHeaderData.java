package com.github.gwiman.mini_mes_backend.quote.application;

/**
 * 타 모듈(salesorder)에서 견적 헤더 핵심 정보만 조회할 때 사용하는 애플리케이션 레이어 전용 타입.
 * api.dto.QuoteResponse 직접 참조를 차단하기 위해 도입.
 */
public record QuoteHeaderData(Long id, String statusCode, Long partnerId, Long employeeId) {}
