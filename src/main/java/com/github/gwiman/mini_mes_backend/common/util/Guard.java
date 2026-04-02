package com.github.gwiman.mini_mes_backend.common.util;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;

/**
 * 서비스 계층의 선행 조건(precondition) 검증 유틸리티.
 * <p>
 * if-throw 블록을 한 줄로 줄여 서비스 로직의 가독성을 높인다.
 * 조건이 false이면 예외를 던지는 방식으로 동작한다.
 * </p>
 */
public final class Guard {

	private Guard() {}

	/**
	 * 조건이 false이면 {@link BusinessRuleViolationException}을 던진다.
	 *
	 * <pre>
	 * // Before
	 * if (!condition) { throw new BusinessRuleViolationException("메시지"); }
	 * // After
	 * Guard.require(condition, "메시지");
	 * </pre>
	 */
	public static void require(boolean condition, String message) {
		if (!condition) throw new BusinessRuleViolationException(message);
	}

	/** 메시지 생성 비용이 큰 경우(문자열 연결 등) 지연 평가용 오버로드 */
	public static void require(boolean condition, Supplier<String> messageSupplier) {
		if (!condition) throw new BusinessRuleViolationException(messageSupplier.get());
	}

	/**
	 * 중복 존재 여부가 true이면 {@link BusinessRuleViolationException}을 던진다.
	 *
	 * <pre>
	 * // Before
	 * if (repository.existsByXxx(value)) { throw new BusinessRuleViolationException("메시지"); }
	 * // After
	 * Guard.requireNotExists(repository.existsByXxx(value), "메시지");
	 * </pre>
	 */
	public static void requireNotExists(boolean exists, String message) {
		if (exists) throw new BusinessRuleViolationException(message);
	}

	/** 메시지 생성 비용이 큰 경우(문자열 연결 등) 지연 평가용 오버로드 */
	public static void requireNotExists(boolean exists, Supplier<String> messageSupplier) {
		if (exists) throw new BusinessRuleViolationException(messageSupplier.get());
	}

	/**
	 * 대상이 존재하지 않으면(false) {@link ResourceNotFoundException}을 던진다.
	 *
	 * <pre>
	 * // Before
	 * if (!repository.existsById(id)) { throw new ResourceNotFoundException("메시지"); }
	 * // After
	 * Guard.requireExists(repository.existsById(id), "메시지");
	 * </pre>
	 */
	public static void requireExists(boolean exists, String message) {
		if (!exists) throw new ResourceNotFoundException(message);
	}

	/** 메시지 생성 비용이 큰 경우(문자열 연결 등) 지연 평가용 오버로드 */
	public static void requireExists(boolean exists, Supplier<String> messageSupplier) {
		if (!exists) throw new ResourceNotFoundException(messageSupplier.get());
	}

	/**
	 * Optional이 비어 있으면 {@link ResourceNotFoundException}을 던지고, 값이 있으면 반환한다.
	 *
	 * <pre>
	 * // Before
	 * Entity e = repository.findById(id)
	 *     .orElseThrow(() -> new ResourceNotFoundException("메시지: " + id));
	 * // After
	 * Entity e = Guard.requireFound(repository.findById(id), "메시지: " + id);
	 * </pre>
	 */
	public static <T> T requireFound(Optional<T> optional, String message) {
		return optional.orElseThrow(() -> new ResourceNotFoundException(message));
	}
}
