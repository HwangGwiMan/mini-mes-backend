package com.github.gwiman.mini_mes_backend.warehouse.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
	@NotBlank(message = "코드는 필수입니다.")
	@Size(max = 50, message = "코드는 50자 이하여야 합니다.")
	String code,

	@NotBlank(message = "창고명은 필수입니다.")
	@Size(max = 100, message = "창고명은 100자 이하여야 합니다.")
	String name,

	@Size(max = 20, message = "창고유형 코드는 20자 이하여야 합니다.")
	String warehouseTypeCode,

	@Size(max = 200, message = "설명은 200자 이하여야 합니다.")
	String description,

	@NotNull(message = "사용여부는 필수입니다.")
	Boolean useYn,

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	int sortOrder
) {}
