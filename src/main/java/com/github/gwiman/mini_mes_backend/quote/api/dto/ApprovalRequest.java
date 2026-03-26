package com.github.gwiman.mini_mes_backend.quote.api.dto;

import jakarta.validation.constraints.Size;

public record ApprovalRequest(
	@Size(max = 500, message = "코멘트는 500자 이하여야 합니다.")
	String comment
) {}
