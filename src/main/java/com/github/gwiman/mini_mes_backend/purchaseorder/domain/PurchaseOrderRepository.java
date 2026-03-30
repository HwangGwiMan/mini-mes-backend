package com.github.gwiman.mini_mes_backend.purchaseorder.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

	@Query("SELECT DISTINCT po FROM PurchaseOrder po LEFT JOIN FETCH po.lines WHERE po.id = :id")
	Optional<PurchaseOrder> findByIdWithLines(@Param("id") Long id);
}
