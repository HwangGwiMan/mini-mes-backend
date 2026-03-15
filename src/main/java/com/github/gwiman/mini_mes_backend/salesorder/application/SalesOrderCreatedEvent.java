package com.github.gwiman.mini_mes_backend.salesorder.application;

/**
 * 수주 생성 완료 이벤트.
 * 출하 도메인이 이 이벤트를 수신해 출하 계획을 자동 생성한다.
 */
public record SalesOrderCreatedEvent(Long salesOrderId) {}
