package com.github.gwiman.mini_mes_backend.workorder.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작업지시 헤더 엔티티.
 * <p>
 * 수주 품목을 기반으로 BOM을 전개하여 생산 계획을 수립한다.
 * 확정(CONFIRMED) 시 투입 자재를 재고에서 선점(MATERIAL_RESERVE)한다.
 * 취소(CANCELLED) 시 선점된 자재를 해제(MATERIAL_UNRESERVE)한다.
 * 상태 흐름: DRAFT → CONFIRMED / CANCELLED
 *             CONFIRMED → CANCELLED
 * </p>
 */
@Entity
@Table(name = "work_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkOrder extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String workOrderNumber;

	/** 연결 수주 ID — 독립 생성 시 null */
	@Column(name = "sales_order_id")
	private Long salesOrderId;

	/** 연결 수주 라인 ID — 독립 생성 시 null */
	@Column(name = "sales_order_line_id")
	private Long salesOrderLineId;

	/** 생산 품목 ID */
	@Column(nullable = false)
	private Long itemId;

	/** 사용 BOM ID (생성 시점 스냅샷) */
	@Column(nullable = false)
	private Long bomId;

	/** 자재 출고 및 예약 기준 창고 ID */
	@Column(nullable = false)
	private Long warehouseId;

	/** 계획 생산 수량 */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal plannedQty;

	@Column(name = "status_code", length = 20, nullable = false)
	private WorkOrderStatus status;

	@Column(nullable = false)
	private LocalDate plannedStartDate;

	private LocalDate plannedEndDate;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<WorkOrderMaterial> materials = new ArrayList<>();

	@OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<WorkOrderRouting> routings = new ArrayList<>();

	private WorkOrder(String workOrderNumber, Long salesOrderId, Long salesOrderLineId,
			Long itemId, Long bomId, Long warehouseId, BigDecimal plannedQty,
			LocalDate plannedStartDate, LocalDate plannedEndDate, String remarks) {
		this.workOrderNumber = workOrderNumber;
		this.salesOrderId = salesOrderId;
		this.salesOrderLineId = salesOrderLineId;
		this.itemId = itemId;
		this.bomId = bomId;
		this.warehouseId = warehouseId;
		this.plannedQty = plannedQty;
		this.status = WorkOrderStatus.DRAFT;
		this.plannedStartDate = plannedStartDate;
		this.plannedEndDate = plannedEndDate;
		this.remarks = remarks != null ? remarks : "";
	}

	/** 작업지시 생성 — 항상 DRAFT로 시작 */
	public static WorkOrder create(String workOrderNumber, Long salesOrderId, Long salesOrderLineId,
			Long itemId, Long bomId, Long warehouseId, BigDecimal plannedQty,
			LocalDate plannedStartDate, LocalDate plannedEndDate, String remarks) {
		return new WorkOrder(workOrderNumber, salesOrderId, salesOrderLineId,
				itemId, bomId, warehouseId, plannedQty, plannedStartDate, plannedEndDate, remarks);
	}

	/** 수정 — DRAFT 상태만 허용 */
	public void update(Long salesOrderId, Long salesOrderLineId, Long itemId, Long bomId,
			Long warehouseId, BigDecimal plannedQty,
			LocalDate plannedStartDate, LocalDate plannedEndDate, String remarks) {
		if (!canEdit()) {
			throw new BusinessRuleViolationException("초안 상태에서만 작업지시를 수정할 수 있습니다.");
		}
		this.salesOrderId = salesOrderId;
		this.salesOrderLineId = salesOrderLineId;
		this.itemId = itemId;
		this.bomId = bomId;
		this.warehouseId = warehouseId;
		this.plannedQty = plannedQty;
		this.plannedStartDate = plannedStartDate;
		this.plannedEndDate = plannedEndDate;
		this.remarks = remarks != null ? remarks : "";
	}

	/** DRAFT → CONFIRMED (자재 선점은 서비스 레이어에서 처리) */
	public void confirm() {
		if (this.status != WorkOrderStatus.DRAFT) {
			throw new BusinessRuleViolationException("초안 상태에서만 작업지시를 확정할 수 있습니다.");
		}
		this.status = WorkOrderStatus.CONFIRMED;
	}

	/** DRAFT / CONFIRMED → CANCELLED (자재 해제는 서비스 레이어에서 처리) */
	public void cancel() {
		if (this.status == WorkOrderStatus.CANCELLED) {
			throw new BusinessRuleViolationException("이미 취소된 작업지시입니다.");
		}
		this.status = WorkOrderStatus.CANCELLED;
	}

	public void addMaterial(WorkOrderMaterial material) {
		materials.add(material);
	}

	public void clearMaterials() {
		materials.clear();
	}

	public void addRouting(WorkOrderRouting routing) {
		routings.add(routing);
	}

	public void clearRoutings() {
		routings.clear();
	}

	public boolean canEdit() {
		return this.status == WorkOrderStatus.DRAFT;
	}

	public boolean canDelete() {
		return this.status == WorkOrderStatus.DRAFT;
	}

	public boolean isConfirmed() {
		return this.status == WorkOrderStatus.CONFIRMED;
	}
}
