package com.github.gwiman.mini_mes_backend.inventory.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 품목×창고×LOT 단위 현재고 응답 DTO.
 * inventory_lot 테이블과 item 테이블을 조인한 결과.
 */
public record InventoryLotResponse(
        Long warehouseId,
        Long itemId,
        String itemCode,
        String itemName,
        String lotNo,
        BigDecimal qtyOnHand,
        BigDecimal qtyReserved,
        BigDecimal availableQty,
        LocalDate expiryDate
) {}
