package com.github.gwiman.mini_mes_backend.item.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {

	@NotBlank(message = "코드는 필수입니다.")
	@Size(max = 50, message = "코드는 50자 이하여야 합니다.")
	private String code;

	@NotBlank(message = "명칭은 필수입니다.")
	@Size(max = 100, message = "명칭은 100자 이하여야 합니다.")
	private String name;
}
