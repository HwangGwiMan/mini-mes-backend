package com.github.gwiman.mini_mes_backend.workorder.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Bom;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.Process;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;
import com.github.gwiman.mini_mes_backend.jooq.tables.Warehouse;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderMaterialResponse;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderResponse;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderRoutingResponse;

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

    // --- work_order_routing 테이블 ---
    private static final Table<?> WOR = DSL.table("work_order_routing");
    private static final Field<Long>    WOR_ID            = DSL.field("work_order_routing.id",             Long.class);
    private static final Field<Long>    WOR_WORK_ORDER_ID = DSL.field("work_order_routing.work_order_id",  Long.class);
    private static final Field<Long>    WOR_ROUTING_ID    = DSL.field("work_order_routing.routing_id",     Long.class);
    private static final Field<Long>    WOR_PROCESS_ID    = DSL.field("work_order_routing.process_id",     Long.class);
    private static final Field<Integer> WOR_STEP_ORDER    = DSL.field("work_order_routing.step_order",     Integer.class);
    private static final Field<Integer> WOR_STANDARD_TIME = DSL.field("work_order_routing.standard_time",  Integer.class);
    private static final Field<String>  WOR_REMARKS       = DSL.field("work_order_routing.remarks",        String.class);

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
                .map(WorkOrderResponse::fromRecord);
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
                .map(WorkOrderMaterialResponse::fromRecord);

        // 라우팅 공정 단계 조회
        Process proc = Process.PROCESS;
        List<WorkOrderRoutingResponse> routings = dsl
                .select(WOR_ID, WOR_ROUTING_ID, WOR_PROCESS_ID,
                        proc.CODE.as("proc_code"), proc.NAME.as("proc_name"),
                        WOR_STEP_ORDER, WOR_STANDARD_TIME, WOR_REMARKS)
                .from(WOR)
                .join(proc).on(WOR_PROCESS_ID.eq(proc.ID))
                .where(WOR_WORK_ORDER_ID.eq(id))
                .orderBy(WOR_STEP_ORDER)
                .fetch()
                .map(WorkOrderRoutingResponse::fromRecord);

        return Optional.of(WorkOrderResponse.fromRecord(header, materials, routings));
    }
}
