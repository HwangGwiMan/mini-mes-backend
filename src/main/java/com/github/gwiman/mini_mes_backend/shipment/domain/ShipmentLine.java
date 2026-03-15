package com.github.gwiman.mini_mes_backend.shipment.domain;

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
 * 출하 라인 엔티티.
 * 수주 라인을 기반으로 생성되며, 계획수량(수주 기반)과 실출하수량을 분리 관리한다.
 * 부분 출하를 허용하므로 actualQuantity &lt;= plannedQuantity 제약은 강제하지 않는다.
 */
@Entity
@Table(name = "shipment_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shipment_id", nullable = false)
	private Shipment shipment;

	/** 원본 수주 라인 참조 */
	@Column(name = "sales_order_line_id", nullable = false)
	private Long salesOrderLineId;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	/** 수주 수량에서 복사된 출하 계획 수량 */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal plannedQuantity;

	/** 실출하수량 — 출하 완료 처리 시 입력 */
	@Column(precision = 19, scale = 4)
	private BigDecimal actualQuantity;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal unitPrice;

	/** 계획 금액 = plannedQuantity * unitPrice */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal plannedAmount;

	/** 실출하 금액 = actualQuantity * unitPrice */
	@Column(precision = 19, scale = 4)
	private BigDecimal actualAmount;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	public ShipmentLine(Shipment shipment, Long salesOrderLineId, Long itemId,
		BigDecimal plannedQuantity, BigDecimal unitPrice, BigDecimal plannedAmount,
		String remarks, int sortOrder) {
		this.shipment = shipment;
		this.salesOrderLineId = salesOrderLineId;
		this.itemId = itemId;
		this.plannedQuantity = plannedQuantity;
		this.unitPrice = unitPrice;
		this.plannedAmount = plannedAmount;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}

	/** 출하 완료 처리 시 실출하수량과 실출하금액을 기록 */
	public void complete(BigDecimal actualQuantity, BigDecimal actualAmount) {
		this.actualQuantity = actualQuantity;
		this.actualAmount = actualAmount;
	}

	/** 출하 계획 수정 — 계획수량 변경 시 계획금액 재계산 */
	public void updatePlan(BigDecimal plannedQuantity, BigDecimal plannedAmount, String remarks) {
		this.plannedQuantity = plannedQuantity;
		this.plannedAmount = plannedAmount;
		this.remarks = remarks;
	}
}
