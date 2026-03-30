package com.github.gwiman.mini_mes_backend.goodsreceipt.api;

import java.math.BigDecimal;

public record GoodsReceiptLineResponse(
		Long id,
		Long itemId,
		String itemCode,
		String itemName,
		Long poLineId,
		String receiptTypeCode,
		BigDecimal receivedQuantity,
		BigDecimal unitPrice,
		String remarks,
		int sortOrder
) {
}
