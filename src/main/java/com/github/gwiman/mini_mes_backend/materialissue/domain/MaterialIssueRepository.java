package com.github.gwiman.mini_mes_backend.materialissue.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 자재 출고 레포지토리.
 */
public interface MaterialIssueRepository extends JpaRepository<MaterialIssue, Long> {

	/** 라인 포함 조회 — 확정/수정 시 N+1 쿼리 방지 */
	@Query("SELECT mi FROM MaterialIssue mi LEFT JOIN FETCH mi.lines WHERE mi.id = :id")
	Optional<MaterialIssue> findByIdWithLines(@Param("id") Long id);

	/** 작업지시 ID로 중복 출고 여부 확인 — 1:1 제약 보장 */
	boolean existsByWorkOrderId(Long workOrderId);

	Optional<MaterialIssue> findByWorkOrderId(Long workOrderId);
}
