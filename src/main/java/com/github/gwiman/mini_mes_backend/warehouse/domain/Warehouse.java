package com.github.gwiman.mini_mes_backend.warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 창고 기준정보 — 재고 관리의 기본 단위 */
@Entity
@Table(name = "warehouse")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	/** 공통코드 WAREHOUSE_TYPE 그룹 참조 */
	@Column(length = 20)
	private String warehouseTypeCode;

	@Column(length = 200)
	private String description;

	@Column(nullable = false)
	private Boolean useYn = true;

	private int sortOrder;

	public Warehouse(String code, String name, String warehouseTypeCode,
		String description, Boolean useYn, int sortOrder) {
		this.code = code;
		this.name = name;
		this.warehouseTypeCode = warehouseTypeCode;
		this.description = description;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}

	public void update(String code, String name, String warehouseTypeCode,
		String description, Boolean useYn, int sortOrder) {
		this.code = code;
		this.name = name;
		this.warehouseTypeCode = warehouseTypeCode;
		this.description = description;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}
}
