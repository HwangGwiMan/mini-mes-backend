package com.github.gwiman.mini_mes_backend.process.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Process {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 20)
	private String processTypeCode;

	private Integer standardTime;

	@Column(length = 200)
	private String description;

	private int sortOrder;

	public Process(String code, String name, String processTypeCode,
		Integer standardTime, String description, int sortOrder) {
		this.code = code;
		this.name = name;
		this.processTypeCode = processTypeCode;
		this.standardTime = standardTime;
		this.description = description;
		this.sortOrder = sortOrder;
	}

	public void update(String code, String name, String processTypeCode,
		Integer standardTime, String description, int sortOrder) {
		this.code = code;
		this.name = name;
		this.processTypeCode = processTypeCode;
		this.standardTime = standardTime;
		this.description = description;
		this.sortOrder = sortOrder;
	}
}
