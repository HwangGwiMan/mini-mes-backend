package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrderLine;

public record SalesOrderLineResponse(
	Long id,
	Long itemId,
	String itemCode,
	String itemName,
	BigDecimal quantity,
	BigDecimal unitPrice,
	BigDecimal amount,
	LocalDate deliveryRequestDate,
	String remarks,
	int sortOrder
) {
	public static SalesOrderLineResponse from(com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderLine entity) {
		return new SalesOrderLineResponse(
			entity.getId(),
			entity.getItemId(),
			null,
			null,
			entity.getQuantity(),
			entity.getUnitPrice(),
			entity.getAmount(),
			entity.getDeliveryRequestDate(),
			entity.getRemarks(),
			entity.getSortOrder()
		);
	}

	public static SalesOrderLineResponse fromRecord(Record r) {
		SalesOrderLine sol = SalesOrderLine.SALES_ORDER_LINE;
		Item i = Item.ITEM;
		return new SalesOrderLineResponse(
			r.get(sol.ID),
			r.get(sol.ITEM_ID),
			r.get(i.CODE),
			r.get(i.NAME),
			r.get(sol.QUANTITY),
			r.get(sol.UNIT_PRICE),
			r.get(sol.AMOUNT),
			r.get(sol.DELIVERY_REQUEST_DATE),
			r.get(sol.REMARKS) != null ? r.get(sol.REMARKS) : "",
			r.get(sol.SORT_ORDER) != null ? r.get(sol.SORT_ORDER) : 0
		);
	}
}
