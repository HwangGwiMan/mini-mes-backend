package com.github.gwiman.mini_mes_backend.salesorder.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

	@Query("SELECT so FROM SalesOrder so "
		+ "LEFT JOIN FETCH so.partner "
		+ "LEFT JOIN FETCH so.employee "
		+ "LEFT JOIN FETCH so.quote "
		+ "LEFT JOIN FETCH so.lines l "
		+ "LEFT JOIN FETCH l.item "
		+ "WHERE so.id = :id")
	Optional<SalesOrder> findByIdWithLines(@Param("id") Long id);

	boolean existsByQuoteId(Long quoteId);
}
