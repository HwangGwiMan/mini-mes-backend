package com.github.gwiman.mini_mes_backend.quote.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteLine;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuoteLineResponse {

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

	public QuoteLineResponse(Long id, Long itemId, String itemCode, String itemName,
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

	public static QuoteLineResponse from(QuoteLine entity) {
		return new QuoteLineResponse(
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

	/**
	 * jOOQ Record를 QuoteLineResponse로 매핑 (QuoteLine + Item 조인 결과용).
	 */
	public static QuoteLineResponse fromRecord(Record r) {
		com.github.gwiman.mini_mes_backend.jooq.tables.QuoteLine ql = com.github.gwiman.mini_mes_backend.jooq.tables.QuoteLine.QUOTE_LINE;
		Item i = Item.ITEM;
		return new QuoteLineResponse(
			r.get(ql.ID),
			r.get(ql.ITEM_ID),
			r.get(i.CODE),
			r.get(i.NAME),
			r.get(ql.QUANTITY),
			r.get(ql.UNIT_PRICE),
			r.get(ql.AMOUNT),
			r.get(ql.DELIVERY_REQUEST_DATE),
			r.get(ql.REMARKS) != null ? r.get(ql.REMARKS) : "",
			r.get(ql.SORT_ORDER) != null ? r.get(ql.SORT_ORDER) : 0
		);
	}
}
