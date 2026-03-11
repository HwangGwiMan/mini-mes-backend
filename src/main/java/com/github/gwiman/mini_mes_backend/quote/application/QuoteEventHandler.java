package com.github.gwiman.mini_mes_backend.quote.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.quote.domain.QuoteRepository;
import com.github.gwiman.mini_mes_backend.salesorder.application.QuoteConvertedToOrderEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuoteEventHandler {

	private static final String QUOTE_STATUS_ORDERED = "QUOTE_STATUS_05";

	private final QuoteRepository quoteRepository;

	@ApplicationModuleListener
	@Transactional
	public void on(QuoteConvertedToOrderEvent event) {
		quoteRepository.findById(event.quoteId())
			.ifPresent(quote -> quote.updateStatus(QUOTE_STATUS_ORDERED));
	}
}
