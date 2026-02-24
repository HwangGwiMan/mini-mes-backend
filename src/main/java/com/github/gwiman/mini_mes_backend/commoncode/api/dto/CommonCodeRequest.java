package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonCodeRequest {

	private String codeGroup;
	private String code;
	private String name;
}
