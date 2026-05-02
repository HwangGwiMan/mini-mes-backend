package com.github.gwiman.mini_mes_backend.quote.application;

/**
 * 견적 승인 요청이 제출될 때 발행되는 이벤트.
 * notification 모듈의 핸들러가 수신해 approverId 기반으로 승인권자에게 알림을 생성한다.
 *
 * @param quoteId        견적 PK
 * @param quoteNumber    견적 문서번호 — 알림 메시지에 포함
 * @param approverId     승인권자 사원 PK — 알림 수신자 결정에 사용
 * @param quoterUsername 제출자 username (Quote.createdBy)
 */
public record QuoteSubmittedEvent(Long quoteId, String quoteNumber, Long approverId, String quoterUsername) {}
