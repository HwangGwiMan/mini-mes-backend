package com.github.gwiman.mini_mes_backend.quote.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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
}
