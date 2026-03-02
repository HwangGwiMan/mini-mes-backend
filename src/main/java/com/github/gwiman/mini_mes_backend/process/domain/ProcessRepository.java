package com.github.gwiman.mini_mes_backend.process.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessRepository extends JpaRepository<Process, Long> {

	@Query("SELECT p FROM Process p WHERE "
		+ "(:code IS NULL OR p.code LIKE %:code% ESCAPE '\\') AND "
		+ "(:name IS NULL OR p.name LIKE %:name% ESCAPE '\\')"
		+ " ORDER BY p.sortOrder ASC, p.code ASC")
	List<Process> search(@Param("code") String code, @Param("name") String name);

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, Long id);
}
