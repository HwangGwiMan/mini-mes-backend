package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrderLine;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SalesOrderLineResponse {

	private Long id;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal amount;
	private LocalDate deliveryRequestDate;
	private String remarks;
	private int sortOrder;

	public SalesOrderLineResponse(Long id, Long itemId, String itemCode, String itemName,
		BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount,
		LocalDate deliveryRequestDate, String remarks, int sortOrder) {
		this.id = id;
		this.itemId = itemId;
		this.itemCode = itemCode;
		this.itemName = itemName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.deliveryRequestDate = deliveryRequestDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	public static SalesOrderLineResponse from(com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderLine entity) {
		return new SalesOrderLineResponse(
			entity.getId(),
			entity.getItem().getId(),
			entity.getItem().getCode(),
			entity.getItem().getName(),
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
