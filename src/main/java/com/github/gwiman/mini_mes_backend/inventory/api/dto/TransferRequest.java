package com.github.gwiman.mini_mes_backend.inventory.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 창고 간 재고 이동 요청 */
public record TransferRequest(
        @NotNull Long fromWarehouseId,
        @NotNull Long toWarehouseId,
        @NotNull Long itemId,
        String lotNo,
        @NotNull @Positive BigDecimal qty
) {}
