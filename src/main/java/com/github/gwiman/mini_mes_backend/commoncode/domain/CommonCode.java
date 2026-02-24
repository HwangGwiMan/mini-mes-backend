package com.github.gwiman.mini_mes_backend.commoncode.domain;

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

	private String codeGroup;
	private String code;
	private String name;

	public CommonCode(String codeGroup, String code, String name) {
		this.codeGroup = codeGroup;
		this.code = code;
		this.name = name;
	}
}
