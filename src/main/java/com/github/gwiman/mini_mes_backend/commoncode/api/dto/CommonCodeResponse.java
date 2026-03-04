package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCode;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonCodeResponse {

	private Long id;
	private String codeGroup;
	private String code;
	private String name;
	private int sortOrder;

	public CommonCodeResponse(Long id, String codeGroup, String code, String name, int sortOrder) {
		this.id = id;
		this.codeGroup = codeGroup;
		this.code = code;
		this.name = name;
		this.sortOrder = sortOrder;
	}

	public static CommonCodeResponse from(CommonCode entity) {
		return new CommonCodeResponse(
			entity.getId(),
			entity.getCodeGroup(),
			entity.getCode(),
			entity.getName(),
			entity.getSortOrder()
		);
	}

	public static CommonCodeResponse fromRecord(Record r) {
		com.github.gwiman.mini_mes_backend.jooq.tables.CommonCode c = com.github.gwiman.mini_mes_backend.jooq.tables.CommonCode.COMMON_CODE;
		return new CommonCodeResponse(
			r.get(c.ID),
			r.get(c.CODE_GROUP),
			r.get(c.CODE),
			r.get(c.NAME),
			r.get(c.SORT_ORDER) != null ? r.get(c.SORT_ORDER) : 0
		);
	}
}
