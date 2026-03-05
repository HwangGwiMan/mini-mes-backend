package com.github.gwiman.mini_mes_backend.salesorder.domain;

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

import com.github.gwiman.mini_mes_backend.item.domain.Item;

@Entity
@Table(name = "sales_order_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesOrderLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sales_order_id", nullable = false)
	private SalesOrder salesOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

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

	public SalesOrderLine(SalesOrder salesOrder, Item item, BigDecimal quantity,
		BigDecimal unitPrice, BigDecimal amount, LocalDate deliveryRequestDate,
		String remarks, int sortOrder) {
		this.salesOrder = salesOrder;
		this.item = item;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.deliveryRequestDate = deliveryRequestDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	public void update(Item item, BigDecimal quantity, BigDecimal unitPrice,
		BigDecimal amount, LocalDate deliveryRequestDate, String remarks, int sortOrder) {
		this.item = item;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.deliveryRequestDate = deliveryRequestDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}
}
