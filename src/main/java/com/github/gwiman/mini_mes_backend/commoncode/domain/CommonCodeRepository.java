package com.github.gwiman.mini_mes_backend.commoncode.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

	List<CommonCode> findByCodeGroupAndUseYnTrueOrderBySortOrder(String codeGroup);
}
