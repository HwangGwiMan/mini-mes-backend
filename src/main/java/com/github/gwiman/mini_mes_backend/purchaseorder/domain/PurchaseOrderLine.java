package com.github.gwiman.mini_mes_backend.purchaseorder.domain;

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

/**
 * 구매 발주 라인 엔티티.
 * <p>
 * 구매 요청(PR) 전환 시 prLineId에 원본 PR 라인 ID를 기록하여 추적 가능성을 유지한다.
 * </p>
 */
@Entity
@Table(name = "purchase_order_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_order_id", nullable = false)
	private PurchaseOrder purchaseOrder;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal orderedQuantity;

	@Column(precision = 19, scale = 4)
	private BigDecimal unitPrice;

	/** 희망 납기일 */
	private LocalDate requiredDate;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	/** 구매 요청 전환 시 원본 PR 라인 ID — 직접 생성 시 null */
	@Column(name = "pr_line_id")
	private Long prLineId;

	private PurchaseOrderLine(PurchaseOrder purchaseOrder, Long itemId,
			BigDecimal orderedQuantity, BigDecimal unitPrice,
			LocalDate requiredDate, String remarks, int sortOrder, Long prLineId) {
		this.purchaseOrder = purchaseOrder;
		this.itemId = itemId;
		this.orderedQuantity = orderedQuantity;
		this.unitPrice = unitPrice;
		this.requiredDate = requiredDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
		this.prLineId = prLineId;
	}

	public static PurchaseOrderLine of(PurchaseOrder purchaseOrder, Long itemId,
			BigDecimal orderedQuantity, BigDecimal unitPrice,
			LocalDate requiredDate, String remarks, int sortOrder, Long prLineId) {
		return new PurchaseOrderLine(purchaseOrder, itemId, orderedQuantity, unitPrice,
				requiredDate, remarks, sortOrder, prLineId);
	}
}
