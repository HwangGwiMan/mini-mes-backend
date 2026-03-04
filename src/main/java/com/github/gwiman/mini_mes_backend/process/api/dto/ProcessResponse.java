package com.github.gwiman.mini_mes_backend.process.api.dto;

import org.jooq.Record;

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

	public static ProcessResponse fromRecord(Record r) {
		com.github.gwiman.mini_mes_backend.jooq.tables.Process p = com.github.gwiman.mini_mes_backend.jooq.tables.Process.PROCESS;
		return new ProcessResponse(
			r.get(p.ID),
			r.get(p.CODE),
			r.get(p.NAME),
			r.get(p.PROCESS_TYPE_CODE),
			r.get(p.STANDARD_TIME),
			r.get(p.DESCRIPTION),
			r.get(p.SORT_ORDER) != null ? r.get(p.SORT_ORDER) : 0
		);
	}
}
