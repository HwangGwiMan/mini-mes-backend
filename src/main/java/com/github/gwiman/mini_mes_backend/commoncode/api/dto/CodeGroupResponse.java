package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CodeGroupResponse {

	private Long id;
	private String groupCode;
	private String groupName;
	private int sortOrder;

	public CodeGroupResponse(Long id, String groupCode, String groupName, int sortOrder) {
		this.id = id;
		this.groupCode = groupCode;
		this.groupName = groupName;
		this.sortOrder = sortOrder;
	}

	public static CodeGroupResponse from(CodeGroup entity) {
		return new CodeGroupResponse(
			entity.getId(),
			entity.getGroupCode(),
			entity.getGroupName(),
			entity.getSortOrder()
		);
	}
}
