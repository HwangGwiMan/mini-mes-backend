package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CodeGroupRequest {

	@NotBlank(message = "그룹코드는 필수입니다.")
	@Size(max = 50, message = "그룹코드는 50자 이하여야 합니다.")
	private String groupCode;

	@NotBlank(message = "그룹명은 필수입니다.")
	@Size(max = 100, message = "그룹명은 100자 이하여야 합니다.")
	private String groupName;

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	private int sortOrder;
}
