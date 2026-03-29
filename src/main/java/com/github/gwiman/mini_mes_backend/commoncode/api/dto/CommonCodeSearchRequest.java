package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CommonCodeSearchRequest(
	@NotBlank(message = "그룹코드는 필수입니다.")
	String groupCode
) {}
