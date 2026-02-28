package com.github.gwiman.mini_mes_backend.partner.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

	@Query("SELECT p FROM Partner p WHERE "
		+ "(:code IS NULL OR p.code LIKE %:code% ESCAPE '\\') AND "
		+ "(:name IS NULL OR p.name LIKE %:name% ESCAPE '\\')")
	List<Partner> search(@Param("code") String code, @Param("name") String name);
}
