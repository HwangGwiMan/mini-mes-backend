package com.github.gwiman.mini_mes_backend.bom.api.dto;

import java.math.BigDecimal;

import org.jooq.Record;

public record BomLineResponse(
	Long id,
	Long materialItemId,
	String materialItemCode,
	String materialItemName,
	/** 해당 자재 품목이 별도 BOM을 가지고 있으면 true — 프론트의 [상세▶] 버튼 노출 기준 */
	boolean hasBom,
	BigDecimal quantity,
	String unit,
	String remarks,
	int sortOrder
) {
	/** jOOQ 조회 결과를 BomLineResponse로 변환. hasBom은 별도 존재 여부 조회 결과를 전달받는다. */
	public static BomLineResponse fromRecord(Record r, boolean hasBom) {
		return new BomLineResponse(
			r.get("id", Long.class),
			r.get("material_item_id", Long.class),
			r.get("material_item_code", String.class),
			r.get("material_item_name", String.class),
			hasBom,
			r.get("quantity", BigDecimal.class),
			r.get("unit", String.class),
			r.get("remarks", String.class),
			r.get("sort_order", Integer.class)
		);
	}
}
