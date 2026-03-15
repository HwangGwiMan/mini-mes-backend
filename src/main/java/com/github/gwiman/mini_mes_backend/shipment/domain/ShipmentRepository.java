package com.github.gwiman.mini_mes_backend.shipment.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 출하 JPA 리포지토리 — 쓰기 작업 및 단순 조회 담당.
 * 복잡한 읽기 쿼리는 ShipmentQueryRepository(jOOQ)에서 처리한다.
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

	@Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.lines WHERE s.id = :id")
	Optional<Shipment> findByIdWithLines(@Param("id") Long id);

	boolean existsBySalesOrderId(Long salesOrderId);
}
