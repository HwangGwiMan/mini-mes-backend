package com.github.gwiman.mini_mes_backend.inventory.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
}
