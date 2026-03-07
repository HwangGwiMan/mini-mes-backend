package com.github.gwiman.mini_mes_backend.quote.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteApprovalRepository extends JpaRepository<QuoteApproval, Long> {

	List<QuoteApproval> findByQuoteIdOrderByCreatedAtAsc(Long quoteId);
}
