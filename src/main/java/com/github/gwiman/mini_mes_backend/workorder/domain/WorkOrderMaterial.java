package com.github.gwiman.mini_mes_backend.workorder.domain;

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
 * 작업지시 투입 자재 라인 엔티티.
 * <p>
 * 작업지시 생성 시 BOM을 전개하여 생성되는 스냅샷 레코드.
 * plannedQty = BOM 자재 수량 × 작업지시 계획 생산 수량.
 * </p>
 */
@Entity
@Table(name = "work_order_material")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkOrderMaterial {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_order_id", nullable = false)
	private WorkOrder workOrder;

	/** 투입 자재 품목 ID */
	@Column(name = "material_item_id", nullable = false)
	private Long materialItemId;

	/** 자재 예약 및 출고 기준 창고 ID */
	@Column(nullable = false)
	private Long warehouseId;

	/** 계획 투입 수량 (BOM qty × 작업지시 plannedQty) */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal plannedQty;

	private int sortOrder;

	private WorkOrderMaterial(WorkOrder workOrder, Long materialItemId,
			Long warehouseId, BigDecimal plannedQty, int sortOrder) {
		this.workOrder = workOrder;
		this.materialItemId = materialItemId;
		this.warehouseId = warehouseId;
		this.plannedQty = plannedQty;
		this.sortOrder = sortOrder;
	}

	public static WorkOrderMaterial of(WorkOrder workOrder, Long materialItemId,
			Long warehouseId, BigDecimal plannedQty, int sortOrder) {
		return new WorkOrderMaterial(workOrder, materialItemId, warehouseId, plannedQty, sortOrder);
	}
}
