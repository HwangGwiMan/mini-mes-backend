package com.github.gwiman.mini_mes_backend.purchaserequest.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum PurchaseRequestStatus {
	DRAFT("PR_STATUS_01"),
	UNDER_REVIEW("PR_STATUS_02"),
	APPROVED("PR_STATUS_03"),
	REJECTED("PR_STATUS_04"),
	ORDERED("PR_STATUS_05");

	private final String code;

	PurchaseRequestStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static PurchaseRequestStatus from(String code) {
		for (PurchaseRequestStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown PurchaseRequestStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<PurchaseRequestStatus, String> {
		@Override
		public String convertToDatabaseColumn(PurchaseRequestStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public PurchaseRequestStatus convertToEntityAttribute(String code) {
			return code == null ? null : PurchaseRequestStatus.from(code);
		}
	}
}
