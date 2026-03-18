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

	private SalesOrderLine(SalesOrder salesOrder, Long itemId, BigDecimal quantity,
		BigDecimal unitPrice, BigDecimal amount, LocalDate deliveryRequestDate,
		String remarks, int sortOrder) {
		this.salesOrder = salesOrder;
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.deliveryRequestDate = deliveryRequestDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	/** amount = quantity * unitPrice 계산을 내부에서 처리 */
	public static SalesOrderLine of(SalesOrder salesOrder, Long itemId, BigDecimal quantity,
		BigDecimal unitPrice, LocalDate deliveryRequestDate, String remarks, int sortOrder) {
		return new SalesOrderLine(salesOrder, itemId, quantity, unitPrice,
			quantity.multiply(unitPrice), deliveryRequestDate, remarks, sortOrder);
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
