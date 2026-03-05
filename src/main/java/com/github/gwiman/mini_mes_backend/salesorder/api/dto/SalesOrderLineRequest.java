package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesOrderLineRequest {

	@NotNull(message = "품목은 필수입니다.")
	private Long itemId;

	@NotNull(message = "수량은 필수입니다.")
	private BigDecimal quantity;

	@NotNull(message = "단가는 필수입니다.")
	private BigDecimal unitPrice;

	private LocalDate deliveryRequestDate;

	private String remarks;

	private int sortOrder;
}
