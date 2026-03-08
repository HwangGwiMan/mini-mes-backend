package com.github.gwiman.mini_mes_backend.price.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPriceRepository extends JpaRepository<ItemPrice, Long> {

	boolean existsByItemId(Long itemId);

	Optional<ItemPrice> findByItemId(Long itemId);
}
