package com.github.gwiman.mini_mes_backend.inventory.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {

    Optional<InventoryLot> findByWarehouseIdAndItemIdAndLotNo(Long warehouseId, Long itemId, String lotNo);

    List<InventoryLot> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
}
