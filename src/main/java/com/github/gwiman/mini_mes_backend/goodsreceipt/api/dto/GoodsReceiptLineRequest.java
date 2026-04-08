package com.github.gwiman.mini_mes_backend.goodsreceipt.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GoodsReceiptLineRequest(
		@NotNull Long itemId,
		Long poLineId,
		@NotBlank String receiptTypeCode,
		@NotNull @DecimalMin("0.0001") BigDecimal receivedQuantity,
		BigDecimal unitPrice,
		@Size(max = 200) String remarks,
		int sortOrder
) {
}
