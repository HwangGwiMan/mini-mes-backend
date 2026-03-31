package com.github.gwiman.mini_mes_backend.salesorder.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SalesOrderStatus {
	DRAFT("ORDER_STATUS_01"),
	CONFIRMED("ORDER_STATUS_02"),
	IN_PROGRESS("ORDER_STATUS_03"),
	COMPLETED("ORDER_STATUS_04"),
	CANCELLED("ORDER_STATUS_05");

	private final String code;

	SalesOrderStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static SalesOrderStatus from(String code) {
		for (SalesOrderStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown SalesOrderStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<SalesOrderStatus, String> {
		@Override
		public String convertToDatabaseColumn(SalesOrderStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public SalesOrderStatus convertToEntityAttribute(String code) {
			return code == null ? null : SalesOrderStatus.from(code);
		}
	}
}
