package com.github.gwiman.mini_mes_backend.quote.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

	@Query("SELECT q FROM Quote q " +
		"LEFT JOIN FETCH q.partner " +
		"LEFT JOIN FETCH q.employee " +
		"WHERE (:quoteNumberPattern IS NULL OR q.quoteNumber LIKE :quoteNumberPattern) " +
		"AND (:partnerId IS NULL OR q.partner.id = :partnerId) " +
		"AND (:statusCode IS NULL OR q.statusCode = :statusCode) " +
		"AND (:fromDate IS NULL OR q.quoteDate >= :fromDate) " +
		"AND (:toDate IS NULL OR q.quoteDate <= :toDate) " +
		"ORDER BY q.quoteDate DESC, q.quoteNumber DESC")
	List<Quote> search(
		@Param("quoteNumberPattern") String quoteNumberPattern,
		@Param("partnerId") Long partnerId,
		@Param("statusCode") String statusCode,
		@Param("fromDate") LocalDate fromDate,
		@Param("toDate") LocalDate toDate
	);

	Optional<Quote> findByQuoteNumber(String quoteNumber);

	@Query("SELECT DISTINCT q FROM Quote q " +
		"LEFT JOIN FETCH q.partner " +
		"LEFT JOIN FETCH q.employee " +
		"LEFT JOIN FETCH q.lines l " +
		"LEFT JOIN FETCH l.item " +
		"WHERE q.id = :id")
	Optional<Quote> findByIdWithLines(@Param("id") Long id);

	@Query("SELECT MAX(q.quoteNumber) FROM Quote q WHERE q.quoteNumber LIKE :prefixPattern")
	Optional<String> findMaxQuoteNumberByPrefix(@Param("prefixPattern") String prefixPattern);
}
