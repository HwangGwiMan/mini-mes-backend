package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCode;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonCodeResponse {

	private Long id;
	private String codeGroup;
	private String code;
	private String name;

	public CommonCodeResponse(Long id, String codeGroup, String code, String name) {
		this.id = id;
		this.codeGroup = codeGroup;
		this.code = code;
		this.name = name;
	}

	public static CommonCodeResponse from(CommonCode entity) {
		return new CommonCodeResponse(
			entity.getId(),
			entity.getCodeGroup(),
			entity.getCode(),
			entity.getName()
		);
	}
}
