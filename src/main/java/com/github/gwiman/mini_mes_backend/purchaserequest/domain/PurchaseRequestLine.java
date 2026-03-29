package com.github.gwiman.mini_mes_backend.purchaserequest.domain;

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
 * 구매 요청 라인 엔티티.
 * <p>
 * 단가 없이 품목·수량·소요예정일만 관리한다. 단가는 구매 발주(PurchaseOrder) 전환 시 담당자가 입력한다.
 * </p>
 */
@Entity
@Table(name = "purchase_request_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseRequestLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_request_id", nullable = false)
	private PurchaseRequest purchaseRequest;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal requestedQuantity;

	/** 자재 소요 예정일 — 생산 일정 기준으로 설정 */
	private LocalDate requiredDate;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	private PurchaseRequestLine(PurchaseRequest purchaseRequest, Long itemId,
			BigDecimal requestedQuantity, LocalDate requiredDate, String remarks, int sortOrder) {
		this.purchaseRequest = purchaseRequest;
		this.itemId = itemId;
		this.requestedQuantity = requestedQuantity;
		this.requiredDate = requiredDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	public static PurchaseRequestLine of(PurchaseRequest purchaseRequest, Long itemId,
			BigDecimal requestedQuantity, LocalDate requiredDate, String remarks, int sortOrder) {
		return new PurchaseRequestLine(purchaseRequest, itemId,
				requestedQuantity, requiredDate, remarks, sortOrder);
	}

	public void update(Long itemId, BigDecimal requestedQuantity,
			LocalDate requiredDate, String remarks, int sortOrder) {
		this.itemId = itemId;
		this.requestedQuantity = requestedQuantity;
		this.requiredDate = requiredDate;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}
}
