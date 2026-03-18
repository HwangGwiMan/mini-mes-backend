package com.github.gwiman.mini_mes_backend.revenue.domain;

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
 * 매출 라인 엔티티.
 * 수주 라인을 기반으로 생성되며, 초기값(수량·단가)은 수주 라인에서 복사하되 사용자가 수정 가능하다.
 * salesOrderLineId로 원본 수주 라인을 추적하고, salesOrderId는 표시 목적으로 보관한다.
 */
@Entity
@Table(name = "revenue_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevenueLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "revenue_id", nullable = false)
	private Revenue revenue;

	/** 원본 수주 라인 참조 — 추적용 */
	@Column(name = "sales_order_line_id", nullable = false)
	private Long salesOrderLineId;

	/** 원본 수주 ID — 목록 표시 시 수주번호 조회에 사용 */
	@Column(name = "sales_order_id", nullable = false)
	private Long salesOrderId;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	/** 수주 수량에서 복사된 초기 수량 — 사용자가 수정 가능 */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal unitPrice;

	/** 매출 금액 = quantity × unitPrice */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	public RevenueLine(Revenue revenue, Long salesOrderLineId, Long salesOrderId, Long itemId,
		BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, String remarks, int sortOrder) {
		this.revenue = revenue;
		this.salesOrderLineId = salesOrderLineId;
		this.salesOrderId = salesOrderId;
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	/** 수량·단가 수정 시 금액 재계산 */
	public void update(BigDecimal quantity, BigDecimal unitPrice, String remarks) {
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = quantity.multiply(unitPrice);
		this.remarks = remarks;
	}
}
