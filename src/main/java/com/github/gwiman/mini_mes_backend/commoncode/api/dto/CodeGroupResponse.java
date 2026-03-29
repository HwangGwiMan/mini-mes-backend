package com.github.gwiman.mini_mes_backend.commoncode.api.dto;

import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroup;

public record CodeGroupResponse(Long id, String groupCode, String groupName, int sortOrder) {

	public static CodeGroupResponse from(CodeGroup entity) {
		return new CodeGroupResponse(
			entity.getId(),
			entity.getGroupCode(),
			entity.getGroupName(),
			entity.getSortOrder()
		);
	}
}
