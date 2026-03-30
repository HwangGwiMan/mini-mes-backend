package com.github.gwiman.mini_mes_backend.warehouse.api.dto;

import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import com.github.gwiman.mini_mes_backend.warehouse.domain.Warehouse;

public record WarehouseResponse(
	Long id,
	String code,
	String name,
	String warehouseTypeCode,
	String description,
	Boolean useYn,
	int sortOrder
) {
	public static WarehouseResponse from(Warehouse w) {
		return new WarehouseResponse(
			w.getId(),
			w.getCode(),
			w.getName(),
			w.getWarehouseTypeCode(),
			w.getDescription(),
			w.getUseYn(),
			w.getSortOrder()
		);
	}

	public static WarehouseResponse fromRecord(Record r) {
		return new WarehouseResponse(
			r.get(DSL.field(DSL.name("id"), SQLDataType.BIGINT),          Long.class),
			r.get(DSL.field(DSL.name("code"), SQLDataType.VARCHAR),        String.class),
			r.get(DSL.field(DSL.name("name"), SQLDataType.VARCHAR),        String.class),
			r.get(DSL.field(DSL.name("warehouse_type_code"), SQLDataType.VARCHAR), String.class),
			r.get(DSL.field(DSL.name("description"), SQLDataType.VARCHAR), String.class),
			r.get(DSL.field(DSL.name("use_yn"), SQLDataType.BOOLEAN),      Boolean.class),
			r.get(DSL.field(DSL.name("sort_order"), SQLDataType.INTEGER),  Integer.class) != null
				? r.get(DSL.field(DSL.name("sort_order"), SQLDataType.INTEGER), Integer.class)
				: 0
		);
	}
}
