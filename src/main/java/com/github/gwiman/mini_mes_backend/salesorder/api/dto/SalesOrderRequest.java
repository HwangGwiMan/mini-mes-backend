package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalesOrderRequest(
	@NotNull(message = "수주일자는 필수입니다.")
	LocalDate orderDate,

	LocalDate deliveryDate,

	@NotNull(message = "거래처는 필수입니다.")
	Long partnerId,

	Long employeeId,

	Long quoteId,

	@Size(max = 20, message = "상태 코드는 20자 이하여야 합니다.")
	String statusCode,

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	String remarks,

	@NotEmpty(message = "수주 상세는 최소 1건 이상이어야 합니다.")
	@Valid
	List<SalesOrderLineRequest> lines
) {}
