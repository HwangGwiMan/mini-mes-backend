package com.github.gwiman.mini_mes_backend.commoncode.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {

	List<CodeGroup> findAllByOrderBySortOrderAsc();

	boolean existsByGroupCode(String groupCode);

	boolean existsByGroupCodeAndIdNot(String groupCode, Long id);
}
