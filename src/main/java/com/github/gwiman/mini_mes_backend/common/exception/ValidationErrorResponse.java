package com.github.gwiman.mini_mes_backend.common.exception;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

	private final String message;
	private final List<FieldError> errors = new ArrayList<>();

	public void addFieldError(String field, String message) {
		errors.add(new FieldError(field, message));
	}

	@Getter
	@RequiredArgsConstructor
	public static class FieldError {
		private final String field;
		private final String message;
	}
}
