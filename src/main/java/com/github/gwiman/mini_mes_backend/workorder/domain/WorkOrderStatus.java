package com.github.gwiman.mini_mes_backend.workorder.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum WorkOrderStatus {
	DRAFT("WO_STATUS_01"),
	CONFIRMED("WO_STATUS_02"),
	CANCELLED("WO_STATUS_03");

	private final String code;

	WorkOrderStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static WorkOrderStatus from(String code) {
		for (WorkOrderStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown WorkOrderStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<WorkOrderStatus, String> {
		@Override
		public String convertToDatabaseColumn(WorkOrderStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public WorkOrderStatus convertToEntityAttribute(String code) {
			return code == null ? null : WorkOrderStatus.from(code);
		}
	}
}
