package com.github.gwiman.mini_mes_backend.commoncode.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

	List<CommonCode> findByCodeGroupAndUseYnTrueOrderBySortOrder(String codeGroup);

	boolean existsByCodeGroup(String codeGroup);

	@Query("SELECT c.code FROM CommonCode c WHERE c.codeGroup = :codeGroup")
	List<String> findCodesByCodeGroup(@Param("codeGroup") String codeGroup);

	@Query("SELECT c FROM CommonCode c WHERE "
		+ "(:codeGroup IS NULL OR c.codeGroup = :codeGroup) AND "
		+ "(:code IS NULL OR c.code LIKE %:code%) AND "
		+ "(:name IS NULL OR c.name LIKE %:name%) "
		+ "ORDER BY c.codeGroup ASC, c.sortOrder ASC")
	List<CommonCode> search(
		@Param("codeGroup") String codeGroup,
		@Param("code") String code,
		@Param("name") String name
	);
}
