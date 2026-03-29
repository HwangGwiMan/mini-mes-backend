package com.github.gwiman.mini_mes_backend.item.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemRequest(
	@NotBlank(message = "코드는 필수입니다.")
	@Size(max = 50, message = "코드는 50자 이하여야 합니다.")
	String code,

	@NotBlank(message = "명칭은 필수입니다.")
	@Size(max = 100, message = "명칭은 100자 이하여야 합니다.")
	String name,

	@Size(max = 20, message = "품목유형 코드는 20자 이하여야 합니다.")
	String itemTypeCode,

	@Size(max = 20, message = "단위는 20자 이하여야 합니다.")
	String unit,

	@Size(max = 100, message = "규격은 100자 이하여야 합니다.")
	String spec,

	@Size(max = 200, message = "설명은 200자 이하여야 합니다.")
	String description,

	Boolean useYn,

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	int sortOrder
) {
	public ItemRequest {
		// useYn 미전달 시 기본값 true
		if (useYn == null) useYn = true;
	}
}
