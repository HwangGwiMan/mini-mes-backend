package com.github.gwiman.mini_mes_backend.goodsreceipt.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GoodsReceiptRequest(
		@NotNull LocalDate receiptDate,
		Long poId,
		@NotNull Long warehouseId,
		@NotNull Long partnerId,
		@Size(max = 200) String remarks,
		@NotEmpty @Valid List<GoodsReceiptLineRequest> lines
) {
}
