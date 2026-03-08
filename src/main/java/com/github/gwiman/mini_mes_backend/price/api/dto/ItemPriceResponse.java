package com.github.gwiman.mini_mes_backend.price.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 단가 조회 응답 DTO.
 * name 필드는 itemName과 동일한 값으로 useCrudPage TDto 제약(id + name 필수)을 충족한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPriceResponse {

	private Long id;
	private String name;       // = itemName (useCrudPage 호환)
	private Long itemId;
	private String itemCode;
	private String itemName;
	private BigDecimal unitPrice;
	private String remarks;
	private LocalDateTime updatedAt;

	public static ItemPriceResponse fromRecord(Record r) {
		Item i = Item.ITEM;
		Long id = r.get("ip_id", Long.class);
		Long itemId = r.get("ip_item_id", Long.class);
		BigDecimal unitPrice = r.get("ip_unit_price", BigDecimal.class);
		String remarks = r.get("ip_remarks", String.class);
		LocalDateTime updatedAt = r.get("ip_updated_at", LocalDateTime.class);
		String itemCode = r.get(i.CODE);
		String itemName = r.get(i.NAME);
		return new ItemPriceResponse(id, itemName, itemId, itemCode, itemName, unitPrice, remarks, updatedAt);
	}
}
