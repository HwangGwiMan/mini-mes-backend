package com.github.gwiman.mini_mes_backend.item.api.dto;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.item.domain.Item;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemResponse {

	private Long id;
	private String code;
	private String name;
	private String itemTypeCode;
	private String unit;
	private String spec;
	private String description;
	private boolean useYn;
	private int sortOrder;

	public ItemResponse(Long id, String code, String name, String itemTypeCode, String unit,
		String spec, String description, boolean useYn, int sortOrder) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.itemTypeCode = itemTypeCode;
		this.unit = unit;
		this.spec = spec;
		this.description = description;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}

	public static ItemResponse from(Item entity) {
		return new ItemResponse(
			entity.getId(),
			entity.getCode(),
			entity.getName(),
			entity.getItemTypeCode(),
			entity.getUnit(),
			entity.getSpec(),
			entity.getDescription(),
			entity.getUseYn(),
			entity.getSortOrder()
		);
	}

	public static ItemResponse fromRecord(Record r) {
		com.github.gwiman.mini_mes_backend.jooq.tables.Item i = com.github.gwiman.mini_mes_backend.jooq.tables.Item.ITEM;
		return new ItemResponse(
			r.get(i.ID),
			r.get(i.CODE),
			r.get(i.NAME),
			r.get(i.ITEM_TYPE_CODE),
			r.get(i.UNIT),
			r.get(i.SPEC),
			r.get(i.DESCRIPTION),
			r.get(i.USE_YN) != null ? r.get(i.USE_YN) : true,
			r.get(i.SORT_ORDER) != null ? r.get(i.SORT_ORDER) : 0
		);
	}
}
