package com.github.gwiman.mini_mes_backend.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTxRepository extends JpaRepository<InventoryTx, Long> {
}
