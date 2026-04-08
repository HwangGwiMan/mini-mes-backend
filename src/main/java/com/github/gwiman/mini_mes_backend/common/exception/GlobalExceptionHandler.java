package com.github.gwiman.mini_mes_backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleResourceNotFound(ResourceNotFoundException e) {
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(BusinessRuleViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleBusinessRuleViolation(BusinessRuleViolationException e) {
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
		return new ErrorResponse(e.getMessage());
	}

	/**
	 * 낙관적 락 충돌 — 동시 수정으로 버전 불일치 발생 시.
	 * 재고 스냅샷(inventory/inventory_lot) 동시 업데이트 시 발생할 수 있으며,
	 * 클라이언트는 요청을 재시도해야 한다.
	 */
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
		return new ErrorResponse("다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.");
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Exception e) {
		return new ErrorResponse("서버 오류가 발생했습니다.");
	}
}
