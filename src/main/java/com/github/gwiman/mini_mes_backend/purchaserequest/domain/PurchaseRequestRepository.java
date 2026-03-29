package com.github.gwiman.mini_mes_backend.purchaserequest.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

	@Query("SELECT DISTINCT pr FROM PurchaseRequest pr LEFT JOIN FETCH pr.lines WHERE pr.id = :id")
	Optional<PurchaseRequest> findByIdWithLines(@Param("id") Long id);
}
