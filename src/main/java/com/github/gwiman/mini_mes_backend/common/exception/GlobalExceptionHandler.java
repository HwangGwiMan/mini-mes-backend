package com.github.gwiman.mini_mes_backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ValidationErrorResponse handleValidation(MethodArgumentNotValidException e) {
		ValidationErrorResponse response = new ValidationErrorResponse("입력값을 확인해주세요.");
		e.getBindingResult().getFieldErrors().forEach(err ->
			response.addFieldError(err.getField(), err.getDefaultMessage())
		);
		return response;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Exception e) {
		return new ErrorResponse("서버 오류가 발생했습니다.");
	}
}
