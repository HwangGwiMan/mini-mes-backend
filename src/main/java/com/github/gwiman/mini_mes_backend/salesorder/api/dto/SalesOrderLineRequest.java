package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
	@DecimalMin(value = "0.0001", message = "수량은 0보다 커야 합니다.")
	private BigDecimal quantity;

	@NotNull(message = "단가는 필수입니다.")
	@DecimalMin(value = "0", message = "단가는 0 이상이어야 합니다.")
	private BigDecimal unitPrice;

	private LocalDate deliveryRequestDate;

	@Size(max = 200, message = "비고는 200자 이하여야 합니다.")
	private String remarks;

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	private int sortOrder;
}
