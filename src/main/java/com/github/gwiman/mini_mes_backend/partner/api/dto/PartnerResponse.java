package com.github.gwiman.mini_mes_backend.partner.api.dto;

import com.github.gwiman.mini_mes_backend.partner.domain.Partner;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PartnerResponse {

	private Long id;
	private String code;
	private String name;

	public PartnerResponse(Long id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public static PartnerResponse from(Partner entity) {
		return new PartnerResponse(
			entity.getId(),
			entity.getCode(),
			entity.getName()
		);
	}
}
