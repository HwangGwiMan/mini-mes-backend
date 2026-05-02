package com.github.gwiman.mini_mes_backend.quote.application;

/**
 * 견적이 승인될 때 발행되는 이벤트.
 * notification 모듈의 핸들러가 수신해 견적 담당자(quoterUsername)에게 알림을 생성한다.
 *
 * @param quoteId        견적 PK
 * @param quoteNumber    견적 문서번호 — 알림 메시지에 포함
 * @param quoterUsername 견적 등록자 username (Quote.createdBy)
 */
public record QuoteApprovedEvent(Long quoteId, String quoteNumber, String quoterUsername) {}
