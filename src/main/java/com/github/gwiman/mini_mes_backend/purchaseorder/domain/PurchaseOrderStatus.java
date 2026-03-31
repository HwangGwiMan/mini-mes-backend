package com.github.gwiman.mini_mes_backend.purchaseorder.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum PurchaseOrderStatus {
	DRAFT("PO_STATUS_01"),
	ORDERED("PO_STATUS_02"),
	RECEIVED("PO_STATUS_03"),
	CANCELLED("PO_STATUS_04");

	private final String code;

	PurchaseOrderStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static PurchaseOrderStatus from(String code) {
		for (PurchaseOrderStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown PurchaseOrderStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<PurchaseOrderStatus, String> {
		@Override
		public String convertToDatabaseColumn(PurchaseOrderStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public PurchaseOrderStatus convertToEntityAttribute(String code) {
			return code == null ? null : PurchaseOrderStatus.from(code);
		}
	}
}
