package com.github.gwiman.mini_mes_backend.common.util;

public final class QueryParamEscaper {

	private QueryParamEscaper() {}

	/**
	 * SQL LIKE 검색을 위해 특수문자(%, _, \)를 이스케이프한다.
	 * null 또는 빈 값이면 null을 반환한다.
	 */
	public static String escapeLike(String value) {
		if (value == null || value.isBlank()) return null;
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	/**
	 * SQL LIKE 검색을 위해 특수문자를 이스케이프하고 양쪽에 %를 붙인다.
	 * null 또는 빈 값이면 null을 반환한다.
	 */
	public static String containsLike(String value) {
		if (value == null || value.isBlank()) return null;
		String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + escaped + "%";
	}
}
