package com.github.gwiman.mini_mes_backend.goodsreceipt.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

	@Query("SELECT gr FROM GoodsReceipt gr LEFT JOIN FETCH gr.lines WHERE gr.id = :id")
	Optional<GoodsReceipt> findByIdWithLines(@Param("id") Long id);
}
