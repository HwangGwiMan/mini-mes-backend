package com.github.gwiman.mini_mes_backend.materialissue.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 자재 출고 상태 열거형.
 * <p>
 * DB에는 공통코드 문자열(MI_STATUS_NN)로 저장되며 JpaConverter가 자동 변환한다.
 * 상태 흐름: DRAFT → CONFIRMED / CANCELLED
 * </p>
 */
public enum MaterialIssueStatus {
	DRAFT("MI_STATUS_01"),
	CONFIRMED("MI_STATUS_02"),
	CANCELLED("MI_STATUS_03");

	private final String code;

	MaterialIssueStatus(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static MaterialIssueStatus from(String code) {
		for (MaterialIssueStatus s : values()) {
			if (s.code.equals(code)) return s;
		}
		throw new IllegalArgumentException("Unknown MaterialIssueStatus code: " + code);
	}

	@Converter(autoApply = true)
	public static class JpaConverter implements AttributeConverter<MaterialIssueStatus, String> {
		@Override
		public String convertToDatabaseColumn(MaterialIssueStatus status) {
			return status == null ? null : status.code();
		}

		@Override
		public MaterialIssueStatus convertToEntityAttribute(String code) {
			return code == null ? null : MaterialIssueStatus.from(code);
		}
	}
}
