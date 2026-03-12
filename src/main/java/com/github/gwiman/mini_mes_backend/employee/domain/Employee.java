package com.github.gwiman.mini_mes_backend.employee.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 20)
	private String deptCode;

	@Column(length = 20)
	private String positionCode;

	private LocalDate hireDate;

	@Column(length = 20)
	private String phone;

	@Column(length = 100)
	private String email;

	private boolean useYn = true;

	private int sortOrder;

	public Employee(String code, String name, String deptCode, String positionCode,
		LocalDate hireDate, String phone, String email, boolean useYn, int sortOrder) {
		this.code = code;
		this.name = name;
		this.deptCode = deptCode;
		this.positionCode = positionCode;
		this.hireDate = hireDate;
		this.phone = phone;
		this.email = email;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}

	public void update(String code, String name, String deptCode, String positionCode,
		LocalDate hireDate, String phone, String email, boolean useYn, int sortOrder) {
		this.code = code;
		this.name = name;
		this.deptCode = deptCode;
		this.positionCode = positionCode;
		this.hireDate = hireDate;
		this.phone = phone;
		this.email = email;
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}
}
