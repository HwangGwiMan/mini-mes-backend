package com.github.gwiman.mini_mes_backend.employee.api.dto;

import java.time.LocalDate;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.employee.domain.Employee;

public record EmployeeResponse(
	Long id,
	String code,
	String name,
	String deptCode,
	String positionCode,
	LocalDate hireDate,
	String phone,
	String email,
	boolean useYn,
	int sortOrder
) {
	public static EmployeeResponse from(Employee entity) {
		return new EmployeeResponse(
			entity.getId(),
			entity.getCode(),
			entity.getName(),
			entity.getDeptCode(),
			entity.getPositionCode(),
			entity.getHireDate(),
			entity.getPhone(),
			entity.getEmail(),
			entity.isUseYn(),
			entity.getSortOrder()
		);
	}

	public static EmployeeResponse fromRecord(Record r) {
		com.github.gwiman.mini_mes_backend.jooq.tables.Employee e = com.github.gwiman.mini_mes_backend.jooq.tables.Employee.EMPLOYEE;
		return new EmployeeResponse(
			r.get(e.ID),
			r.get(e.CODE),
			r.get(e.NAME),
			r.get(e.DEPT_CODE),
			r.get(e.POSITION_CODE),
			r.get(e.HIRE_DATE),
			r.get(e.PHONE),
			r.get(e.EMAIL),
			r.get(e.USE_YN) != null && r.get(e.USE_YN),
			r.get(e.SORT_ORDER) != null ? r.get(e.SORT_ORDER) : 0
		);
	}
}
