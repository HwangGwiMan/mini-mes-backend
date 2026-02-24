package com.github.gwiman.mini_mes_backend.item.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
