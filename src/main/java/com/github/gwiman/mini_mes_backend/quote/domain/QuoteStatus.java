package com.github.gwiman.mini_mes_backend.quote.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum QuoteStatus {
	DRAFT("QUOTE_STATUS_01"),
	SUBMITTED("QUOTE_STATUS_02"),
	APPROVED("QUOTE_STATUS_03"),
	REJECTED("QUOTE_STATUS_04"),
	ORDERED("QUOTE_STATUS_05");

	private final String code;

	QuoteStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static QuoteStatus from(String code) {
		for (QuoteStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown QuoteStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<QuoteStatus, String> {
		@Override
		public String convertToDatabaseColumn(QuoteStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public QuoteStatus convertToEntityAttribute(String code) {
			return code == null ? null : QuoteStatus.from(code);
		}
	}
}
