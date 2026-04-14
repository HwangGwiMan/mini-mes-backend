package com.github.gwiman.mini_mes_backend.workorder.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Bom;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;
import com.github.gwiman.mini_mes_backend.jooq.tables.Warehouse;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderMaterialResponse;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderResponse;

import lombok.RequiredArgsConstructor;

/**
 * 작업지시 읽기 전용 쿼리 리포지토리.
 * work_order / work_order_material 테이블은 jOOQ 코드 생성 대상에 없어
 * 원시 DSL로 처리한다. Item, Warehouse, SalesOrder, Bom은 생성 클래스 사용.
 */
@Component
@RequiredArgsConstructor
public class WorkOrderQueryRepository {

    private final DSLContext dsl;

    // --- work_order 테이블 ---
    private static final Table<?> WO = DSL.table("work_order");
    private static final Field<Long>       WO_ID                  = DSL.field("work_order.id",                    Long.class);
    private static final Field<String>     WO_WORK_ORDER_NUMBER   = DSL.field("work_order.work_order_number",     String.class);
    private static final Field<Long>       WO_SALES_ORDER_ID      = DSL.field("work_order.sales_order_id",        Long.class);
    private static final Field<Long>       WO_SALES_ORDER_LINE_ID = DSL.field("work_order.sales_order_line_id",   Long.class);
    private static final Field<Long>       WO_ITEM_ID             = DSL.field("work_order.item_id",               Long.class);
    private static final Field<Long>       WO_BOM_ID              = DSL.field("work_order.bom_id",                Long.class);
    private static final Field<Long>       WO_WAREHOUSE_ID        = DSL.field("work_order.warehouse_id",          Long.class);
    private static final Field<BigDecimal> WO_PLANNED_QTY         = DSL.field("work_order.planned_qty",           BigDecimal.class);
    private static final Field<String>     WO_STATUS_CODE         = DSL.field("work_order.status_code",           String.class);
    private static final Field<LocalDate>  WO_PLANNED_START_DATE  = DSL.field("work_order.planned_start_date",    LocalDate.class);
    private static final Field<LocalDate>  WO_PLANNED_END_DATE    = DSL.field("work_order.planned_end_date",      LocalDate.class);
    private static final Field<String>     WO_REMARKS             = DSL.field("work_order.remarks",               String.class);

    // --- work_order_material 테이블 ---
    private static final Table<?> WOM = DSL.table("work_order_material");
    private static final Field<Long>       WOM_ID               = DSL.field("work_order_material.id",               Long.class);
    private static final Field<Long>       WOM_WORK_ORDER_ID    = DSL.field("work_order_material.work_order_id",    Long.class);
    private static final Field<Long>       WOM_MATERIAL_ITEM_ID = DSL.field("work_order_material.material_item_id", Long.class);
    private static final Field<Long>       WOM_WAREHOUSE_ID     = DSL.field("work_order_material.warehouse_id",     Long.class);
    private static final Field<BigDecimal> WOM_PLANNED_QTY      = DSL.field("work_order_material.planned_qty",      BigDecimal.class);
    private static final Field<Integer>    WOM_SORT_ORDER       = DSL.field("work_order_material.sort_order",       Integer.class);

    /**
     * 작업지시 목록 조회 — workOrderNumber 패턴(containsLike 처리된 값), itemName 패턴, statusCode 필터.
     */
    public List<WorkOrderResponse> search(String workOrderNumberPattern, String itemNamePattern, String statusCode) {
        Item item   = Item.ITEM;
        Warehouse wh = Warehouse.WAREHOUSE;
        SalesOrder so = SalesOrder.SALES_ORDER;
        Bom bom     = Bom.BOM;

        Condition woCond     = workOrderNumberPattern != null
                ? WO_WORK_ORDER_NUMBER.like(workOrderNumberPattern)
                : DSL.noCondition();
        Condition itemCond   = itemNamePattern != null
                ? item.NAME.likeIgnoreCase(itemNamePattern)
                : DSL.noCondition();
        Condition statusCond = statusCode != null && !statusCode.isBlank()
                ? WO_STATUS_CODE.eq(statusCode)
                : DSL.noCondition();

        return dsl
                .select(WO_ID, WO_WORK_ORDER_NUMBER,
                        WO_SALES_ORDER_ID, so.ORDER_NUMBER.as("sales_order_number"),
                        WO_SALES_ORDER_LINE_ID,
                        WO_ITEM_ID, item.CODE.as("item_code"), item.NAME.as("item_name"),
                        WO_BOM_ID, bom.VERSION_CODE.as("bom_version_code"),
                        WO_WAREHOUSE_ID, wh.NAME.as("warehouse_name"),
                        WO_PLANNED_QTY, WO_STATUS_CODE,
                        WO_PLANNED_START_DATE, WO_PLANNED_END_DATE, WO_REMARKS)
                .from(WO)
                .join(item).on(WO_ITEM_ID.eq(item.ID))
                .join(bom).on(WO_BOM_ID.eq(bom.ID))
                .join(wh).on(WO_WAREHOUSE_ID.eq(wh.ID))
                .leftJoin(so).on(WO_SALES_ORDER_ID.eq(so.ID))
                .where(woCond).and(itemCond).and(statusCond)
                .orderBy(WO_ID.desc())
                .fetch()
                .map(r -> new WorkOrderResponse(
                        r.get(WO_ID),
                        r.get(WO_WORK_ORDER_NUMBER),
                        r.get(WO_WORK_ORDER_NUMBER),
                        r.get(WO_SALES_ORDER_ID),
                        r.get(DSL.field("sales_order_number", String.class)),
                        r.get(WO_SALES_ORDER_LINE_ID),
                        r.get(WO_ITEM_ID),
                        r.get(DSL.field("item_code", String.class)),
                        r.get(DSL.field("item_name", String.class)),
                        r.get(WO_BOM_ID),
                        r.get(DSL.field("bom_version_code", String.class)),
                        r.get(WO_WAREHOUSE_ID),
                        r.get(DSL.field("warehouse_name", String.class)),
                        r.get(WO_PLANNED_QTY),
                        r.get(WO_STATUS_CODE),
                        r.get(WO_PLANNED_START_DATE),
                        r.get(WO_PLANNED_END_DATE),
                        r.get(WO_REMARKS),
                        List.of()));
    }

    /**
     * 작업지시 단건 조회 (자재 목록 포함).
     */
    public Optional<WorkOrderResponse> findById(Long id) {
        Item item   = Item.ITEM;
        Item matItem = Item.ITEM.as("mat_item");
        Warehouse wh = Warehouse.WAREHOUSE;
        Warehouse matWh = Warehouse.WAREHOUSE.as("mat_wh");
        SalesOrder so = SalesOrder.SALES_ORDER;
        Bom bom     = Bom.BOM;

        // 헤더 조회
        var header = dsl
                .select(WO_ID, WO_WORK_ORDER_NUMBER,
                        WO_SALES_ORDER_ID, so.ORDER_NUMBER.as("sales_order_number"),
                        WO_SALES_ORDER_LINE_ID,
                        WO_ITEM_ID, item.CODE.as("item_code"), item.NAME.as("item_name"),
                        WO_BOM_ID, bom.VERSION_CODE.as("bom_version_code"),
                        WO_WAREHOUSE_ID, wh.NAME.as("warehouse_name"),
                        WO_PLANNED_QTY, WO_STATUS_CODE,
                        WO_PLANNED_START_DATE, WO_PLANNED_END_DATE, WO_REMARKS)
                .from(WO)
                .join(item).on(WO_ITEM_ID.eq(item.ID))
                .join(bom).on(WO_BOM_ID.eq(bom.ID))
                .join(wh).on(WO_WAREHOUSE_ID.eq(wh.ID))
                .leftJoin(so).on(WO_SALES_ORDER_ID.eq(so.ID))
                .where(WO_ID.eq(id))
                .fetchOne();

        if (header == null) return Optional.empty();

        // 자재 라인 조회
        List<WorkOrderMaterialResponse> materials = dsl
                .select(WOM_ID, WOM_MATERIAL_ITEM_ID,
                        matItem.CODE.as("mat_item_code"), matItem.NAME.as("mat_item_name"),
                        WOM_WAREHOUSE_ID, matWh.NAME.as("mat_wh_name"),
                        WOM_PLANNED_QTY, WOM_SORT_ORDER)
                .from(WOM)
                .join(matItem).on(WOM_MATERIAL_ITEM_ID.eq(matItem.ID))
                .join(matWh).on(WOM_WAREHOUSE_ID.eq(matWh.ID))
                .where(WOM_WORK_ORDER_ID.eq(id))
                .orderBy(WOM_SORT_ORDER)
                .fetch()
                .map(r -> new WorkOrderMaterialResponse(
                        r.get(WOM_ID),
                        r.get(WOM_MATERIAL_ITEM_ID),
                        r.get(DSL.field("mat_item_code", String.class)),
                        r.get(DSL.field("mat_item_name", String.class)),
                        r.get(WOM_WAREHOUSE_ID),
                        r.get(DSL.field("mat_wh_name", String.class)),
                        r.get(WOM_PLANNED_QTY),
                        r.get(WOM_SORT_ORDER)));

        return Optional.of(new WorkOrderResponse(
                header.get(WO_ID),
                header.get(WO_WORK_ORDER_NUMBER),
                header.get(WO_WORK_ORDER_NUMBER),
                header.get(WO_SALES_ORDER_ID),
                header.get(DSL.field("sales_order_number", String.class)),
                header.get(WO_SALES_ORDER_LINE_ID),
                header.get(WO_ITEM_ID),
                header.get(DSL.field("item_code", String.class)),
                header.get(DSL.field("item_name", String.class)),
                header.get(WO_BOM_ID),
                header.get(DSL.field("bom_version_code", String.class)),
                header.get(WO_WAREHOUSE_ID),
                header.get(DSL.field("warehouse_name", String.class)),
                header.get(WO_PLANNED_QTY),
                header.get(WO_STATUS_CODE),
                header.get(WO_PLANNED_START_DATE),
                header.get(WO_PLANNED_END_DATE),
                header.get(WO_REMARKS),
                materials));
    }
}
