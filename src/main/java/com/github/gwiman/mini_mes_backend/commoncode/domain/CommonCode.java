package com.github.gwiman.mini_mes_backend.commoncode.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "common_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String codeGroup;

	@Column(nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean useYn = true;

	public CommonCode(String codeGroup, String code, String name, int sortOrder) {
		this.codeGroup = codeGroup;
		this.code = code;
		this.name = name;
		this.sortOrder = sortOrder;
		this.useYn = true;
	}
}
