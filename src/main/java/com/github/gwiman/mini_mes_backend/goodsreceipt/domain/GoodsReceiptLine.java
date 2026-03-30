package com.github.gwiman.mini_mes_backend.goodsreceipt.domain;

import java.math.BigDecimal;

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
 * 자재 입고 라인 엔티티.
 * <p>
 * receiptTypeCode로 직접입고(GR_LINE_TYPE_01)와 발주입고(GR_LINE_TYPE_02)를 구분한다.
 * 발주 입고 시 poLineId에 원본 PO 라인 ID를 기록하여 추적 가능성을 유지한다.
 * </p>
 */
@Entity
@Table(name = "goods_receipt_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoodsReceiptLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goods_receipt_id", nullable = false)
	private GoodsReceipt goodsReceipt;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	/** 발주 입고 시 원본 PO 라인 ID — 직접 입고 시 null */
	@Column(name = "po_line_id")
	private Long poLineId;

	/** GR_LINE_TYPE 공통코드: GR_LINE_TYPE_01(직접입고) / GR_LINE_TYPE_02(발주입고) */
	@Column(length = 20, nullable = false)
	private String receiptTypeCode;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal receivedQuantity;

	@Column(precision = 19, scale = 4)
	private BigDecimal unitPrice;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	private GoodsReceiptLine(GoodsReceipt goodsReceipt, Long itemId, Long poLineId,
			String receiptTypeCode, BigDecimal receivedQuantity,
			BigDecimal unitPrice, String remarks, int sortOrder) {
		this.goodsReceipt = goodsReceipt;
		this.itemId = itemId;
		this.poLineId = poLineId;
		this.receiptTypeCode = receiptTypeCode;
		this.receivedQuantity = receivedQuantity;
		this.unitPrice = unitPrice;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	public static GoodsReceiptLine of(GoodsReceipt goodsReceipt, Long itemId, Long poLineId,
			String receiptTypeCode, BigDecimal receivedQuantity,
			BigDecimal unitPrice, String remarks, int sortOrder) {
		return new GoodsReceiptLine(goodsReceipt, itemId, poLineId,
				receiptTypeCode, receivedQuantity, unitPrice, remarks, sortOrder);
	}
}
