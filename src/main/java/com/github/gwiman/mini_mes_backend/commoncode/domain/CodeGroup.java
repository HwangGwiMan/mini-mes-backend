package com.github.gwiman.mini_mes_backend.commoncode.domain;

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
@Table(name = "code_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String groupCode;

	@Column(nullable = false, length = 100)
	private String groupName;

	@Column(nullable = false)
	private int sortOrder;

	public CodeGroup(String groupCode, String groupName, int sortOrder) {
		this.groupCode = groupCode;
		this.groupName = groupName;
		this.sortOrder = sortOrder;
	}

	public void update(String groupCode, String groupName, int sortOrder) {
		this.groupCode = groupCode;
		this.groupName = groupName;
		this.sortOrder = sortOrder;
	}
}
