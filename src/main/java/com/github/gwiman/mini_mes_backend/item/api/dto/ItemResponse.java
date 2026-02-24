package com.github.gwiman.mini_mes_backend.item.api.dto;

import com.github.gwiman.mini_mes_backend.item.domain.Item;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemResponse {

	private Long id;
	private String code;
	private String name;

	public ItemResponse(Long id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public static ItemResponse from(Item entity) {
		return new ItemResponse(
			entity.getId(),
			entity.getCode(),
			entity.getName()
		);
	}
}
