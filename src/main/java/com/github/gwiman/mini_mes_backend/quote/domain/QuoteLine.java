package com.github.gwiman.mini_mes_backend.quote.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quote_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quote_id", nullable = false)
	private Quote quote;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal unitPrice;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	private LocalDate deliveryRequestDate;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	public QuoteLine(Quote quote, Long itemId, BigDecimal quantity, BigDecimal unitPrice,
		BigDecimal amount, LocalDate deliveryRequestDate, String remarks, int sortOrder) {
		this.quote = quote;
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.deliveryRequestDate = deliveryRequestDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	public void update(Long itemId, BigDecimal quantity, BigDecimal unitPrice,
		BigDecimal amount, LocalDate deliveryRequestDate, String remarks, int sortOrder) {
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.deliveryRequestDate = deliveryRequestDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}
}
