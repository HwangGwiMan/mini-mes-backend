package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonCodeSearchRequest {

	@NotBlank(message = "그룹코드는 필수입니다.")
	private String groupCode;
}
