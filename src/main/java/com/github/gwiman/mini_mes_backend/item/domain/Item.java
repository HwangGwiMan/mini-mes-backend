package com.github.gwiman.mini_mes_backend.item.domain;

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

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 20)
	private String itemTypeCode;

	@Column(length = 20)
	private String unit;

	@Column(length = 100)
	private String spec;

	@Column(length = 200)
	private String description;

	@Column(nullable = false)
	private Boolean useYn = true;

	@Column()
	private int sortOrder;

	public Item(String code, String name, String itemTypeCode, String unit, String spec,
		String description, boolean useYn, int sortOrder) {
		this.code = code;
		this.name = name;
		this.itemTypeCode = itemTypeCode;
		this.unit = unit;
		this.spec = spec;
		this.description = description;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}

	public void update(String code, String name, String itemTypeCode, String unit, String spec,
		String description, boolean useYn, int sortOrder) {
		this.code = code;
		this.name = name;
		this.itemTypeCode = itemTypeCode;
		this.unit = unit;
		this.spec = spec;
		this.description = description;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}
}
