package com.github.gwiman.mini_mes_backend.quote.application;

/**
 * 견적이 수주로 전환될 때 발행되는 이벤트.
 * salesorder 모듈이 publish하고, quote 모듈의 QuoteEventHandler가 subscribe해 견적 상태를 수주완료로 변경한다.
 * 이벤트를 quote 모듈에 정의한 이유: salesorder 모듈에 두면 quote → salesorder 역방향 의존이 생겨
 * salesorder → quote 의존과 순환이 발생하기 때문이다.
 */
public record QuoteConvertedToOrderEvent(Long quoteId) {}
