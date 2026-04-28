package com.github.gwiman.mini_mes_backend.materialissue.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.Warehouse;
import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueLineResponse;
import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueResponse;

import lombok.RequiredArgsConstructor;

/**
 * 자재 출고 읽기 전용 쿼리 리포지토리.
 * material_issue / material_issue_line / work_order 테이블은 jOOQ 코드 생성 대상에 없어
 * 원시 DSL로 처리한다. Item, Warehouse는 생성 클래스 사용.
 */
@Component
@RequiredArgsConstructor
public class MaterialIssueQueryRepository {

    private final DSLContext dsl;

    // --- material_issue 테이블 ---
    private static final Table<?> MI = DSL.table("material_issue");
    private static final Field<Long>      MI_ID                    = DSL.field("material_issue.id",                     Long.class);
    private static final Field<String>    MI_MATERIAL_ISSUE_NUMBER = DSL.field("material_issue.material_issue_number",   String.class);
    private static final Field<Long>      MI_WORK_ORDER_ID         = DSL.field("material_issue.work_order_id",           Long.class);
    private static final Field<String>    MI_STATUS_CODE           = DSL.field("material_issue.status_code",             String.class);
    private static final Field<LocalDate> MI_ISSUE_DATE            = DSL.field("material_issue.issue_date",              LocalDate.class);
    private static final Field<String>    MI_REMARKS               = DSL.field("material_issue.remarks",                 String.class);

    // --- work_order 테이블 (헤더 조인용) ---
    private static final Table<?> WO = DSL.table("work_order");
    private static final Field<Long>   WO_ID                = DSL.field("work_order.id",                Long.class);
    private static final Field<String> WO_WORK_ORDER_NUMBER = DSL.field("work_order.work_order_number", String.class);
    private static final Field<Long>   WO_ITEM_ID           = DSL.field("work_order.item_id",           Long.class);

    // --- material_issue_line 테이블 ---
    private static final Table<?> MIL = DSL.table("material_issue_line");
    private static final Field<Long>   MIL_ID                    = DSL.field("material_issue_line.id",                     Long.class);
    private static final Field<Long>   MIL_MATERIAL_ISSUE_ID     = DSL.field("material_issue_line.material_issue_id",       Long.class);
    private static final Field<Long>   MIL_WORK_ORDER_MATERIAL_ID = DSL.field("material_issue_line.work_order_material_id", Long.class);
    private static final Field<Long>   MIL_MATERIAL_ITEM_ID      = DSL.field("material_issue_line.material_item_id",        Long.class);
    private static final Field<Long>   MIL_WAREHOUSE_ID          = DSL.field("material_issue_line.warehouse_id",            Long.class);
    private static final Field<String> MIL_LOT_NO                = DSL.field("material_issue_line.lot_no",                  String.class);
    private static final Field<java.math.BigDecimal> MIL_ISSUED_QTY = DSL.field("material_issue_line.issued_qty",           java.math.BigDecimal.class);
    private static final Field<Integer> MIL_SORT_ORDER           = DSL.field("material_issue_line.sort_order",              Integer.class);

    /**
     * 자재 출고 목록 조회 — 번호 패턴, 작업지시 번호 패턴, 상태 필터.
     */
    public List<MaterialIssueResponse> search(String miNumberPattern, String woNumberPattern, String statusCode) {
        Item item = Item.ITEM;

        Condition miCond     = miNumberPattern != null
                ? MI_MATERIAL_ISSUE_NUMBER.like(miNumberPattern)
                : DSL.noCondition();
        Condition woCond     = woNumberPattern != null
                ? WO_WORK_ORDER_NUMBER.like(woNumberPattern)
                : DSL.noCondition();
        Condition statusCond = statusCode != null && !statusCode.isBlank()
                ? MI_STATUS_CODE.eq(statusCode)
                : DSL.noCondition();

        return dsl
                .select(MI_ID, MI_MATERIAL_ISSUE_NUMBER,
                        MI_WORK_ORDER_ID, WO_WORK_ORDER_NUMBER.as("work_order_number"),
                        WO_ITEM_ID.as("item_id"), item.NAME.as("item_name"),
                        MI_STATUS_CODE, MI_ISSUE_DATE, MI_REMARKS)
                .from(MI)
                .join(WO).on(MI_WORK_ORDER_ID.eq(WO_ID))
                .join(item).on(WO_ITEM_ID.eq(item.ID))
                .where(miCond).and(woCond).and(statusCond)
                .orderBy(MI_ID.desc())
                .fetch()
                .map(MaterialIssueResponse::fromRecord);
    }

    /**
     * 자재 출고 단건 조회 (라인 목록 포함).
     */
    public Optional<MaterialIssueResponse> findById(Long id) {
        Item item     = Item.ITEM;
        Item matItem  = Item.ITEM.as("mat_item");
        Warehouse matWh = Warehouse.WAREHOUSE.as("mat_wh");

        // 헤더 조회
        var header = dsl
                .select(MI_ID, MI_MATERIAL_ISSUE_NUMBER,
                        MI_WORK_ORDER_ID, WO_WORK_ORDER_NUMBER.as("work_order_number"),
                        WO_ITEM_ID.as("item_id"), item.NAME.as("item_name"),
                        MI_STATUS_CODE, MI_ISSUE_DATE, MI_REMARKS)
                .from(MI)
                .join(WO).on(MI_WORK_ORDER_ID.eq(WO_ID))
                .join(item).on(WO_ITEM_ID.eq(item.ID))
                .where(MI_ID.eq(id))
                .fetchOne();

        if (header == null) return Optional.empty();

        // 라인 조회
        List<MaterialIssueLineResponse> lines = dsl
                .select(MIL_ID, MIL_WORK_ORDER_MATERIAL_ID,
                        MIL_MATERIAL_ITEM_ID,
                        matItem.CODE.as("item_code"), matItem.NAME.as("item_name"),
                        MIL_WAREHOUSE_ID, matWh.NAME.as("warehouse_name"),
                        MIL_LOT_NO, MIL_ISSUED_QTY, MIL_SORT_ORDER)
                .from(MIL)
                .join(matItem).on(MIL_MATERIAL_ITEM_ID.eq(matItem.ID))
                .join(matWh).on(MIL_WAREHOUSE_ID.eq(matWh.ID))
                .where(MIL_MATERIAL_ISSUE_ID.eq(id))
                .orderBy(MIL_SORT_ORDER)
                .fetch()
                .map(MaterialIssueLineResponse::fromRecord);

        return Optional.of(MaterialIssueResponse.fromRecord(header, lines));
    }
}
