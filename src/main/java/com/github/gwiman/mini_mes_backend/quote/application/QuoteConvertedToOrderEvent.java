package com.github.gwiman.mini_mes_backend.quote.application;

/**
 * 견적이 수주로 전환될 때 발행되는 이벤트.
 * 발행 주체인 salesorder 모듈이 publish, quote 모듈이 subscribe해 견적 상태를 수주완료로 변경.
 */
public record QuoteConvertedToOrderEvent(Long quoteId) {}
