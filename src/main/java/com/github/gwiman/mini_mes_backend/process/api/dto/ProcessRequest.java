package com.github.gwiman.mini_mes_backend.process.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcessRequest {

	@NotBlank(message = "코드는 필수입니다.")
	@Size(max = 50, message = "코드는 50자 이하여야 합니다.")
	private String code;

	@NotBlank(message = "공정명은 필수입니다.")
	@Size(max = 100, message = "공정명은 100자 이하여야 합니다.")
	private String name;

	@Size(max = 20, message = "공정유형 코드는 20자 이하여야 합니다.")
	private String processTypeCode;

	private Integer standardTime;

	@Size(max = 200, message = "설명은 200자 이하여야 합니다.")
	private String description;

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	private int sortOrder;
}
