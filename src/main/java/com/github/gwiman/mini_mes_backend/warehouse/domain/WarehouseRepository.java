package com.github.gwiman.mini_mes_backend.warehouse.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

	@Query("SELECT w FROM Warehouse w WHERE "
		+ "(:code IS NULL OR w.code LIKE %:code% ESCAPE '\\') AND "
		+ "(:name IS NULL OR w.name LIKE %:name% ESCAPE '\\') AND "
		+ "(:useYn IS NULL OR w.useYn = :useYn) "
		+ "ORDER BY w.sortOrder ASC, w.code ASC")
	List<Warehouse> search(@Param("code") String code,
		@Param("name") String name,
		@Param("useYn") Boolean useYn);

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, Long id);
}
