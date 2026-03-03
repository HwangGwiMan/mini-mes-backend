package com.github.gwiman.mini_mes_backend.employee.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	@Query("SELECT e FROM Employee e WHERE "
		+ "(:code IS NULL OR e.code LIKE %:code% ESCAPE '\\') AND "
		+ "(:name IS NULL OR e.name LIKE %:name% ESCAPE '\\') AND "
		+ "(:deptCode IS NULL OR e.deptCode = :deptCode) "
		+ "ORDER BY e.sortOrder ASC, e.code ASC")
	List<Employee> search(@Param("code") String code, @Param("name") String name, @Param("deptCode") String deptCode);

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, Long id);
}
