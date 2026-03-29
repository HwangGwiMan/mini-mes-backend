package com.github.gwiman.mini_mes_backend.purchaserequest.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchaseRequestRequest(
	@NotNull(message = "요청일자는 필수입니다.")
	LocalDate requestDate,

	Long requesterId,

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	String remarks,

	@NotEmpty(message = "구매 요청 상세는 최소 1건 이상이어야 합니다.")
	@Valid
	List<PurchaseRequestLineRequest> lines
) {}
