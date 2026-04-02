package com.github.gwiman.mini_mes_backend.goodsreceipt.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * 자재 입고 확정(COMPLETED) 시 발행.
 * 직접입고/발주입고 모두 포함 — poId 유무와 무관하게 항상 발행된다.
 * inventory 모듈이 수신하여 재고(qty_on_hand)를 반영한다.
 */
public record StockReceivedEvent(
		Long goodsReceiptId,
		Long warehouseId,
		List<Line> lines
) {
	/**
	 * @param itemId      품목 ID
	 * @param lotNo       LOT 번호 — 현재 입고 단계에서는 null (자재 출고 시 LOT 지정)
	 * @param receivedQty 입고 수량
	 */
	public record Line(Long itemId, String lotNo, BigDecimal receivedQty) {}
}
