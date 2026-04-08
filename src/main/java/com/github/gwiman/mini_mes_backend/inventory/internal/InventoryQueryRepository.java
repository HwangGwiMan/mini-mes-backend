package com.github.gwiman.mini_mes_backend.inventory.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryLotResponse;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryResponse;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryTxResponse;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;

import lombok.RequiredArgsConstructor;

/**
 * 재고 원장 읽기 전용 쿼리 리포지토리.
 * inventory/inventory_lot/inventory_tx 테이블은 jOOQ 코드 생성 대상에 포함되지 않아
 * 원시 DSL로 처리한다. Item은 기존 jOOQ 생성 클래스를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class InventoryQueryRepository {

    private final DSLContext dsl;

    // --- inventory 테이블 ---
    private static final Table<?> INV = DSL.table("inventory");
    private static final Field<Long>       INV_WAREHOUSE_ID = DSL.field("inventory.warehouse_id",  Long.class);
    private static final Field<Long>       INV_ITEM_ID      = DSL.field("inventory.item_id",       Long.class);
    private static final Field<BigDecimal> INV_QTY_ON_HAND  = DSL.field("inventory.qty_on_hand",   BigDecimal.class);
    private static final Field<BigDecimal> INV_QTY_RESERVED = DSL.field("inventory.qty_reserved",  BigDecimal.class);

    // --- inventory_lot 테이블 ---
    private static final Table<?> LOT = DSL.table("inventory_lot");
    private static final Field<Long>       LOT_WAREHOUSE_ID = DSL.field("inventory_lot.warehouse_id", Long.class);
    private static final Field<Long>       LOT_ITEM_ID      = DSL.field("inventory_lot.item_id",      Long.class);
    private static final Field<String>     LOT_LOT_NO       = DSL.field("inventory_lot.lot_no",       String.class);
    private static final Field<BigDecimal> LOT_QTY_ON_HAND  = DSL.field("inventory_lot.qty_on_hand",  BigDecimal.class);
    private static final Field<BigDecimal> LOT_QTY_RESERVED = DSL.field("inventory_lot.qty_reserved", BigDecimal.class);
    private static final Field<LocalDate>  LOT_EXPIRY_DATE  = DSL.field("inventory_lot.expiry_date",  LocalDate.class);

    // --- inventory_tx 테이블 ---
    private static final Table<?> TX = DSL.table("inventory_tx");
    private static final Field<Long>          TX_ID           = DSL.field("inventory_tx.id",           Long.class);
    private static final Field<Long>          TX_WAREHOUSE_ID = DSL.field("inventory_tx.warehouse_id", Long.class);
    private static final Field<Long>          TX_ITEM_ID      = DSL.field("inventory_tx.item_id",      Long.class);
    private static final Field<String>        TX_LOT_NO       = DSL.field("inventory_tx.lot_no",       String.class);
    private static final Field<String>        TX_TX_TYPE      = DSL.field("inventory_tx.tx_type",      String.class);
    private static final Field<BigDecimal>    TX_QTY_DELTA    = DSL.field("inventory_tx.qty_delta",    BigDecimal.class);
    private static final Field<String>        TX_REF_TYPE     = DSL.field("inventory_tx.ref_type",     String.class);
    private static final Field<Long>          TX_REF_ID       = DSL.field("inventory_tx.ref_id",       Long.class);
    private static final Field<LocalDate>     TX_TX_DATE      = DSL.field("inventory_tx.tx_date",      LocalDate.class);
    private static final Field<LocalDateTime> TX_CREATED_AT   = DSL.field("inventory_tx.created_at",   LocalDateTime.class);
    private static final Field<String>        TX_CREATED_BY   = DSL.field("inventory_tx.created_by",   String.class);

    /**
     * 현재고 목록 조회 — 창고 ID, 품목 ID로 필터링.
     */
    public List<InventoryResponse> searchInventory(Long warehouseId, Long itemId) {
        Item item = Item.ITEM;

        Condition whCond   = warehouseId != null ? INV_WAREHOUSE_ID.eq(warehouseId) : DSL.noCondition();
        Condition itemCond = itemId      != null ? INV_ITEM_ID.eq(itemId)            : DSL.noCondition();

        return dsl
                .select(INV_WAREHOUSE_ID, INV_ITEM_ID,
                        item.CODE.as("item_code"), item.NAME.as("item_name"),
                        INV_QTY_ON_HAND, INV_QTY_RESERVED)
                .from(INV)
                .join(item).on(INV_ITEM_ID.eq(item.ID))
                .where(whCond).and(itemCond)
                .orderBy(INV_WAREHOUSE_ID, INV_ITEM_ID)
                .fetch()
                .map(r -> {
                    BigDecimal onHand   = r.get(INV_QTY_ON_HAND);
                    BigDecimal reserved = r.get(INV_QTY_RESERVED);
                    return new InventoryResponse(
                            r.get(INV_WAREHOUSE_ID),
                            r.get(INV_ITEM_ID),
                            r.get(DSL.field("item_code", String.class)),
                            r.get(DSL.field("item_name", String.class)),
                            onHand,
                            reserved,
                            onHand.subtract(reserved));
                });
    }

    /**
     * LOT별 현재고 목록 조회 — 창고 ID, 품목 ID로 필터링.
     */
    public List<InventoryLotResponse> searchInventoryLots(Long warehouseId, Long itemId) {
        Item item = Item.ITEM;

        Condition whCond   = warehouseId != null ? LOT_WAREHOUSE_ID.eq(warehouseId) : DSL.noCondition();
        Condition itemCond = itemId      != null ? LOT_ITEM_ID.eq(itemId)            : DSL.noCondition();

        return dsl
                .select(LOT_WAREHOUSE_ID, LOT_ITEM_ID,
                        item.CODE.as("item_code"), item.NAME.as("item_name"),
                        LOT_LOT_NO, LOT_QTY_ON_HAND, LOT_QTY_RESERVED, LOT_EXPIRY_DATE)
                .from(LOT)
                .join(item).on(LOT_ITEM_ID.eq(item.ID))
                .where(whCond).and(itemCond)
                .orderBy(LOT_WAREHOUSE_ID, LOT_ITEM_ID, LOT_LOT_NO)
                .fetch()
                .map(r -> {
                    BigDecimal onHand   = r.get(LOT_QTY_ON_HAND);
                    BigDecimal reserved = r.get(LOT_QTY_RESERVED);
                    return new InventoryLotResponse(
                            r.get(LOT_WAREHOUSE_ID),
                            r.get(LOT_ITEM_ID),
                            r.get(DSL.field("item_code", String.class)),
                            r.get(DSL.field("item_name", String.class)),
                            r.get(LOT_LOT_NO),
                            onHand,
                            reserved,
                            onHand.subtract(reserved),
                            r.get(LOT_EXPIRY_DATE));
                });
    }

    /**
     * 수불 이력 조회 — 창고·품목·기간으로 필터링, 최신 순 정렬.
     */
    public List<InventoryTxResponse> searchTxHistory(Long warehouseId, Long itemId,
            LocalDate fromDate, LocalDate toDate) {
        Item item = Item.ITEM;

        Condition whCond   = warehouseId != null ? TX_WAREHOUSE_ID.eq(warehouseId) : DSL.noCondition();
        Condition itemCond = itemId      != null ? TX_ITEM_ID.eq(itemId)            : DSL.noCondition();
        Condition fromCond = fromDate    != null ? TX_TX_DATE.ge(fromDate)          : DSL.noCondition();
        Condition toCond   = toDate      != null ? TX_TX_DATE.le(toDate)            : DSL.noCondition();

        return dsl
                .select(TX_ID, TX_WAREHOUSE_ID, TX_ITEM_ID,
                        item.CODE.as("item_code"), item.NAME.as("item_name"),
                        TX_LOT_NO, TX_TX_TYPE, TX_QTY_DELTA,
                        TX_REF_TYPE, TX_REF_ID, TX_TX_DATE,
                        TX_CREATED_AT, TX_CREATED_BY)
                .from(TX)
                .join(item).on(TX_ITEM_ID.eq(item.ID))
                .where(whCond).and(itemCond).and(fromCond).and(toCond)
                .orderBy(TX_ID.desc())
                .fetch()
                .map(r -> new InventoryTxResponse(
                        r.get(TX_ID),
                        r.get(TX_WAREHOUSE_ID),
                        r.get(TX_ITEM_ID),
                        r.get(DSL.field("item_code", String.class)),
                        r.get(DSL.field("item_name", String.class)),
                        r.get(TX_LOT_NO),
                        r.get(TX_TX_TYPE),
                        r.get(TX_QTY_DELTA),
                        r.get(TX_REF_TYPE),
                        r.get(TX_REF_ID),
                        r.get(TX_TX_DATE),
                        r.get(TX_CREATED_AT),
                        r.get(TX_CREATED_BY)));
    }
}
