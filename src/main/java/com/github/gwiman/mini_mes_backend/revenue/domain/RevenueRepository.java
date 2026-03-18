package com.github.gwiman.mini_mes_backend.revenue.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueRepository extends JpaRepository<Revenue, Long> {

	@EntityGraph(attributePaths = "lines")
	Optional<Revenue> findWithLinesById(Long id);
}
