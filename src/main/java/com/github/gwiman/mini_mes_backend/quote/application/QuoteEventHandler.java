package com.github.gwiman.mini_mes_backend.quote.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.quote.domain.QuoteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuoteEventHandler {

	private static final String QUOTE_STATUS_ORDERED = "QUOTE_STATUS_05";

	private final QuoteRepository quoteRepository;

	// @ApplicationModuleListener는 내부적으로 @TransactionalEventListener를 포함하므로 @Transactional 중복 불가
	@ApplicationModuleListener
	public void on(QuoteConvertedToOrderEvent event) {
		quoteRepository.findById(event.quoteId())
			.ifPresent(quote -> quote.updateStatus(QUOTE_STATUS_ORDERED));
	}
}
