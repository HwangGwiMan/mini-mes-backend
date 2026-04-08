package com.github.gwiman.mini_mes_backend.inventory.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 수불 이력 원장 응답 DTO.
 * inventory_tx 테이블과 item 테이블을 조인한 결과.
 */
public record InventoryTxResponse(
        Long id,
        Long warehouseId,
        Long itemId,
        String itemCode,
        String itemName,
        String lotNo,
        String txType,
        BigDecimal qtyDelta,
        String refType,
        Long refId,
        LocalDate txDate,
        LocalDateTime createdAt,
        String createdBy
) {}
