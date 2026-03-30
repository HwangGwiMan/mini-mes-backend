package com.github.gwiman.mini_mes_backend.bom.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BomRepository extends JpaRepository<Bom, Long> {

	boolean existsByItemIdAndVersionCode(Long itemId, String versionCode);

	List<Bom> findByItemId(Long itemId);

	@Query("SELECT DISTINCT b FROM Bom b LEFT JOIN FETCH b.lines WHERE b.id = :id")
	Optional<Bom> findByIdWithLines(@Param("id") Long id);
}
