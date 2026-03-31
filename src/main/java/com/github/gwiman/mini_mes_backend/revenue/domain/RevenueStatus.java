package com.github.gwiman.mini_mes_backend.revenue.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum RevenueStatus {
	DRAFT("REVENUE_STATUS_01"),
	CLOSED("REVENUE_STATUS_02"),
	CANCELLED("REVENUE_STATUS_03");

	private final String code;

	RevenueStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static RevenueStatus from(String code) {
		for (RevenueStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown RevenueStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<RevenueStatus, String> {
		@Override
		public String convertToDatabaseColumn(RevenueStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public RevenueStatus convertToEntityAttribute(String code) {
			return code == null ? null : RevenueStatus.from(code);
		}
	}
}
