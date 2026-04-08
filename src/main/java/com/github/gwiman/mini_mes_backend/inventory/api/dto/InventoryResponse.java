package com.github.gwiman.mini_mes_backend.inventory.api.dto;

import java.math.BigDecimal;

/**
 * 품목×창고 단위 현재고 응답 DTO.
 * inventory 테이블과 item 테이블을 조인한 결과.
 */
public record InventoryResponse(
        Long warehouseId,
        Long itemId,
        String itemCode,
        String itemName,
        BigDecimal qtyOnHand,
        BigDecimal qtyReserved,
        BigDecimal availableQty
) {}
