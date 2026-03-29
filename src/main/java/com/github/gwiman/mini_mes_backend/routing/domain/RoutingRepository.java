package com.github.gwiman.mini_mes_backend.routing.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutingRepository extends JpaRepository<Routing, Long> {

	boolean existsByBomId(Long bomId);

	Optional<Routing> findByBomId(Long bomId);

	@Query("SELECT DISTINCT r FROM Routing r LEFT JOIN FETCH r.steps WHERE r.id = :id")
	Optional<Routing> findByIdWithSteps(@Param("id") Long id);
}
