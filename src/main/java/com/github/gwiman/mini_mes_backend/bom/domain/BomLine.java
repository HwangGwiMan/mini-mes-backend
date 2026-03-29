package com.github.gwiman.mini_mes_backend.bom.domain;

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
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BOM 자재 라인 엔티티.
 * <p>
 * BOM 헤더(Bom) 하나에 속하는 투입 자재 한 행을 나타낸다.
 * materialItemId는 투입 자재 품목의 ID이며, 헤더의 itemId와 동일하면 순환 참조로 간주해 등록을 거부해야 한다.
 * </p>
 */
@Entity
@Table(name = "bom_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BomLine extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bom_id", nullable = false)
	private Bom bom;

	@Column(name = "material_item_id", nullable = false)
	private Long materialItemId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal quantity;

	@Column(length = 20)
	private String unit;

	@Column(length = 200)
	private String remarks;

	private int sortOrder;

	public BomLine(Bom bom, Long materialItemId, BigDecimal quantity, String unit, String remarks, int sortOrder) {
		this.bom = bom;
		this.materialItemId = materialItemId;
		this.quantity = quantity;
		this.unit = unit;
		this.remarks = remarks;
		this.sortOrder = sortOrder;
	}
}
