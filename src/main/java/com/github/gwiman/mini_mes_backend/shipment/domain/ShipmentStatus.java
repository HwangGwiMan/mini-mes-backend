package com.github.gwiman.mini_mes_backend.shipment.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum ShipmentStatus {
	WAITING("SHIPMENT_STATUS_01"),
	IN_PROGRESS("SHIPMENT_STATUS_02"),
	COMPLETED("SHIPMENT_STATUS_03");

	private final String code;

	ShipmentStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static ShipmentStatus from(String code) {
		for (ShipmentStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown ShipmentStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<ShipmentStatus, String> {
		@Override
		public String convertToDatabaseColumn(ShipmentStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public ShipmentStatus convertToEntityAttribute(String code) {
			return code == null ? null : ShipmentStatus.from(code);
		}
	}
}
