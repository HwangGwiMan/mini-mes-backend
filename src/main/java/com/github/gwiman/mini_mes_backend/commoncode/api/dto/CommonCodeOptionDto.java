package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

/**
 * GraphQL {@code commonCodes} 쿼리 전용 경량 응답.
 *
 * <p>화면 초기화 시 셀렉트 박스 옵션으로만 사용되므로 code·name만 포함한다.
 * id·codeGroup·sortOrder 등이 필요한 경우 CRUD용 {@link CommonCodeResponse}를 사용할 것.
 */
public record CommonCodeOptionDto(String code, String name) {}
