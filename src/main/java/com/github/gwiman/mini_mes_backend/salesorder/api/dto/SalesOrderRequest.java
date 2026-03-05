package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesOrderRequest {

	@NotNull(message = "수주일자는 필수입니다.")
	private LocalDate orderDate;

	private LocalDate deliveryDate;

	@NotNull(message = "거래처는 필수입니다.")
	private Long partnerId;

	private Long employeeId;

	private Long quoteId;

	@Size(max = 20, message = "상태 코드는 20자 이하여야 합니다.")
	private String statusCode;

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	private String remarks;

	@NotEmpty(message = "수주 상세는 최소 1건 이상이어야 합니다.")
	@Valid
	private List<SalesOrderLineRequest> lines;
}
