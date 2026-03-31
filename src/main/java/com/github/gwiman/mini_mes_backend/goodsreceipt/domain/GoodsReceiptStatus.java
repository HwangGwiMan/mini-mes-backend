package com.github.gwiman.mini_mes_backend.goodsreceipt.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum GoodsReceiptStatus {
	DRAFT("GR_STATUS_01"),
	COMPLETED("GR_STATUS_02"),
	CANCELLED("GR_STATUS_03");

	private final String code;

	GoodsReceiptStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static GoodsReceiptStatus from(String code) {
		for (GoodsReceiptStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown GoodsReceiptStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<GoodsReceiptStatus, String> {
		@Override
		public String convertToDatabaseColumn(GoodsReceiptStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public GoodsReceiptStatus convertToEntityAttribute(String code) {
			return code == null ? null : GoodsReceiptStatus.from(code);
		}
	}
}
