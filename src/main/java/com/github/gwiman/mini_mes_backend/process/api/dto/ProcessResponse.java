package com.github.gwiman.mini_mes_backend.process.api.dto;

import com.github.gwiman.mini_mes_backend.process.domain.Process;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProcessResponse {

	private Long id;
	private String code;
	private String name;
	private String processTypeCode;
	private Integer standardTime;
	private String description;
	private int sortOrder;

	public ProcessResponse(Long id, String code, String name, String processTypeCode,
		Integer standardTime, String description, int sortOrder) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.processTypeCode = processTypeCode;
		this.standardTime = standardTime;
		this.description = description;
		this.sortOrder = sortOrder;
	}

	public static ProcessResponse from(Process entity) {
		return new ProcessResponse(
			entity.getId(),
			entity.getCode(),
			entity.getName(),
			entity.getProcessTypeCode(),
			entity.getStandardTime(),
			entity.getDescription(),
			entity.getSortOrder()
		);
	}
}
