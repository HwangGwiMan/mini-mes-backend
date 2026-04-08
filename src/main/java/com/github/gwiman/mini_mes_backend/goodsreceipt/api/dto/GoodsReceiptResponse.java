package com.github.gwiman.mini_mes_backend.goodsreceipt.api.dto;

import java.time.LocalDate;
import java.util.List;

public record GoodsReceiptResponse(
		Long id,
		String receiptNumber,
		/** useCrudPage 호환용 — receiptNumber와 동일 */
		String name,
		LocalDate receiptDate,
		Long poId,
		String poNumber,
		Long warehouseId,
		Long partnerId,
		String partnerCode,
		String partnerName,
		String statusCode,
		String remarks,
		List<GoodsReceiptLineResponse> lines
) {
}
