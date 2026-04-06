package com.github.gwiman.mini_mes_backend.inventory.domain;

/**
 * 재고 수불 유형 — ADR-004 tx_type 정의.
 * qty_delta는 항상 양수이며, 방향(입고/출고/선점)은 이 열거형으로 결정한다.
 */
public enum InventoryTxType {

    /** 구매 입고 — qty_on_hand++ */
    PURCHASE_IN,

    /** 작업지시 투입 자재 선점 — qty_reserved++ */
    MATERIAL_RESERVE,

    /** 작업지시 취소 시 선점 해제 — qty_reserved-- */
    MATERIAL_UNRESERVE,

    /** 자재 출고 확정(생산 투입) — qty_on_hand--, qty_reserved-- */
    PRODUCTION_OUT,

    /** 생산 완료 입고 — qty_on_hand++ */
    PRODUCTION_IN,

    /** 출고 확정 — qty_on_hand-- */
    SALES_OUT,

    /** 창고 이동 출고 — qty_on_hand-- */
    TRANSFER_OUT,

    /** 창고 이동 입고 — qty_on_hand++ */
    TRANSFER_IN,

    /** 재고 조정 증가 — qty_on_hand++ */
    ADJUST_IN,

    /** 재고 조정 감소 — qty_on_hand-- */
    ADJUST_OUT
}
