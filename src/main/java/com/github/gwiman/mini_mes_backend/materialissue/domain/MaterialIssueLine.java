package com.github.gwiman.mini_mes_backend.materialissue.domain;

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

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자재 출고 라인 엔티티.
 * <p>
 * 작업지시 생성 시 WorkOrderMaterial에서 복사된 스냅샷.
 * LOT 지정은 선택적이며, 출고 수량은 기본값이 계획 수량이지만 수정 가능하다.
 * </p>
 */
@Entity
@Table(name = "material_issue_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaterialIssueLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "material_issue_id", nullable = false)
	private MaterialIssue materialIssue;

	/** WorkOrderMaterial 역추적용 ID */
	@Column(nullable = false)
	private Long workOrderMaterialId;

	/** 투입 자재 품목 ID (WorkOrderMaterial에서 복사) */
	@Column(nullable = false)
	private Long materialItemId;

	/** 출고 기준 창고 ID (WorkOrderMaterial에서 복사) */
	@Column(nullable = false)
	private Long warehouseId;

	/** LOT 번호 — 선택적 지정 */
	@Column(length = 50)
	private String lotNo;

	/** 실제 출고 수량 — 기본값은 계획 투입 수량 */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal issuedQty;

	private int sortOrder;

	private MaterialIssueLine(MaterialIssue materialIssue, Long workOrderMaterialId,
			Long materialItemId, Long warehouseId, String lotNo,
			BigDecimal issuedQty, int sortOrder) {
		this.materialIssue = materialIssue;
		this.workOrderMaterialId = workOrderMaterialId;
		this.materialItemId = materialItemId;
		this.warehouseId = warehouseId;
		this.lotNo = lotNo;
		this.issuedQty = issuedQty;
		this.sortOrder = sortOrder;
	}

	public static MaterialIssueLine of(MaterialIssue materialIssue, Long workOrderMaterialId,
			Long materialItemId, Long warehouseId, String lotNo,
			BigDecimal issuedQty, int sortOrder) {
		return new MaterialIssueLine(materialIssue, workOrderMaterialId,
				materialItemId, warehouseId, lotNo, issuedQty, sortOrder);
	}

	/** LOT 및 출고 수량 수정 — DRAFT 상태에서만 호출 */
	public void update(String lotNo, BigDecimal issuedQty) {
		if (issuedQty == null || issuedQty.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessRuleViolationException("출고 수량은 양수여야 합니다.");
		}
		this.lotNo = lotNo;
		this.issuedQty = issuedQty;
	}
}
