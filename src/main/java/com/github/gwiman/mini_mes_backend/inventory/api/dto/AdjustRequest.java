package com.github.gwiman.mini_mes_backend.inventory.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 재고 조정 요청 */
public record AdjustRequest(
        @NotNull Long warehouseId,
        @NotNull Long itemId,
        String lotNo,
        @NotNull @Positive BigDecimal qty,
        /** "ADJUST_IN" 또는 "ADJUST_OUT" */
        @NotBlank String txType
) {}
