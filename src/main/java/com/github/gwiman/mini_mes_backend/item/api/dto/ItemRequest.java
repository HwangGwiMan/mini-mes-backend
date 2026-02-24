package com.github.gwiman.mini_mes_backend.item.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {

	private String code;
	private String name;
}
