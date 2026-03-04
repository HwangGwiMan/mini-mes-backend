package com.github.gwiman.mini_mes_backend.employee.infrastructure;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeQueryRepository {

	private final DSLContext dsl;

	public Optional<EmployeeResponse> findById(Long id) {
		Employee e = Employee.EMPLOYEE;
		return dsl
			.selectFrom(e)
			.where(e.ID.eq(id))
			.fetchOptional()
			.map(EmployeeResponse::fromRecord);
	}
}
