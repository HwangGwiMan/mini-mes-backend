package com.github.gwiman.mini_mes_backend.quote.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

	Optional<Quote> findByQuoteNumber(String quoteNumber);

	@Query("SELECT DISTINCT q FROM Quote q LEFT JOIN FETCH q.lines WHERE q.id = :id")
	Optional<Quote> findByIdWithLines(@Param("id") Long id);
}
