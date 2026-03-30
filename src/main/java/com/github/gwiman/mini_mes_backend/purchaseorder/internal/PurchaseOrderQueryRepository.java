package com.github.gwiman.mini_mes_backend.purchaseorder.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderLineResponse;
import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderResponse;

import lombok.RequiredArgsConstructor;

/**
 * 구매 발주 읽기 전용 쿼리 리포지토리.
 * purchase_order/purchase_order_line 테이블은 jOOQ 코드 생성 대상에 아직 포함되지 않아 원시 DSL로 처리한다.
 * Partner, Item은 기존 jOOQ 생성 클래스를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class PurchaseOrderQueryRepository {

	private final DSLContext dsl;

	// 원시 DSL 테이블/필드 정의 — jOOQ 코드 생성 후 제거 예정
	private static final Table<?> PO  = DSL.table("purchase_order");
	private static final Table<?> POL = DSL.table("purchase_order_line");

	private static final Field<Long>      PO_ID               = DSL.field("purchase_order.id",                    Long.class);
	private static final Field<String>    PO_ORDER_NUMBER     = DSL.field("purchase_order.order_number",          String.class);
	private static final Field<LocalDate> PO_ORDER_DATE       = DSL.field("purchase_order.order_date",            LocalDate.class);
	private static final Field<Long>      PO_PARTNER_ID       = DSL.field("purchase_order.partner_id",            Long.class);
	private static final Field<LocalDate> PO_EXPECTED_ARRIVAL = DSL.field("purchase_order.expected_arrival_date", LocalDate.class);
	private static final Field<String>    PO_STATUS_CODE      = DSL.field("purchase_order.status_code",           String.class);
	private static final Field<Long>      PO_PR_ID            = DSL.field("purchase_order.pr_id",                 Long.class);
	private static final Field<String>    PO_REMARKS          = DSL.field("purchase_order.remarks",               String.class);

	private static final Field<Long>        POL_ID            = DSL.field("purchase_order_line.id",                Long.class);
	private static final Field<Long>        POL_PO_ID         = DSL.field("purchase_order_line.purchase_order_id", Long.class);
	private static final Field<Long>        POL_ITEM_ID       = DSL.field("purchase_order_line.item_id",           Long.class);
	private static final Field<BigDecimal>  POL_QTY           = DSL.field("purchase_order_line.ordered_quantity",  BigDecimal.class);
	private static final Field<BigDecimal>  POL_UNIT_PRICE    = DSL.field("purchase_order_line.unit_price",        BigDecimal.class);
	private static final Field<LocalDate>   POL_REQUIRED_DATE = DSL.field("purchase_order_line.required_date",     LocalDate.class);
	private static final Field<String>      POL_REMARKS       = DSL.field("purchase_order_line.remarks",           String.class);
	private static final Field<Integer>     POL_SORT_ORDER    = DSL.field("purchase_order_line.sort_order",        Integer.class);
	private static final Field<Long>        POL_PR_LINE_ID    = DSL.field("purchase_order_line.pr_line_id",        Long.class);

	public List<PurchaseOrderResponse> search(String orderNumberPattern,
			String partnerNamePattern, String statusCode) {
		Partner p = Partner.PARTNER;
		Field<String> partnerName = p.NAME.as("partner_name");

		Condition numberCond  = orderNumberPattern  != null ? PO_ORDER_NUMBER.like(orderNumberPattern)           : DSL.noCondition();
		Condition partnerCond = partnerNamePattern  != null ? p.NAME.likeIgnoreCase(partnerNamePattern)          : DSL.noCondition();
		Condition statusCond  = statusCode          != null ? PO_STATUS_CODE.eq(statusCode)                      : DSL.noCondition();

		return dsl
			.select(PO_ID, PO_ORDER_NUMBER, PO_ORDER_DATE, PO_PARTNER_ID,
					PO_EXPECTED_ARRIVAL, PO_STATUS_CODE, PO_PR_ID, PO_REMARKS, partnerName)
			.from(PO)
			.leftJoin(p).on(PO_PARTNER_ID.eq(p.ID))
			.where(numberCond).and(partnerCond).and(statusCond)
			.orderBy(PO_ID.desc())
			.fetch()
			.map(r -> toResponse(r, List.of()));
	}

	public Optional<PurchaseOrderResponse> findByIdWithLines(Long id) {
		Partner p = Partner.PARTNER;
		Item item = Item.ITEM;
		Field<String> partnerName = p.NAME.as("partner_name");

		// Query 1: 헤더 + 거래처명
		var headerRecord = dsl
			.select(PO_ID, PO_ORDER_NUMBER, PO_ORDER_DATE, PO_PARTNER_ID,
					PO_EXPECTED_ARRIVAL, PO_STATUS_CODE, PO_PR_ID, PO_REMARKS, partnerName)
			.from(PO)
			.leftJoin(p).on(PO_PARTNER_ID.eq(p.ID))
			.where(PO_ID.eq(id))
			.fetchOne();

		if (headerRecord == null) {
			return Optional.empty();
		}

		// Query 2: 라인 + 품목 코드/명
		Field<String> itemCode = item.CODE.as("item_code");
		Field<String> itemName = item.NAME.as("item_name");

		List<PurchaseOrderLineResponse> lines = dsl
			.select(POL_ID, POL_ITEM_ID, POL_QTY, POL_UNIT_PRICE,
					POL_REQUIRED_DATE, POL_REMARKS, POL_SORT_ORDER, POL_PR_LINE_ID,
					itemCode, itemName)
			.from(POL)
			.join(item).on(POL_ITEM_ID.eq(item.ID))
			.where(POL_PO_ID.eq(id))
			.orderBy(POL_SORT_ORDER)
			.fetch()
			.map(r -> new PurchaseOrderLineResponse(
				r.get(POL_ID),
				r.get(POL_ITEM_ID),
				r.get(itemCode),
				r.get(itemName),
				r.get(POL_QTY),
				r.get(POL_UNIT_PRICE),
				r.get(POL_REQUIRED_DATE),
				r.get(POL_REMARKS) != null ? r.get(POL_REMARKS) : "",
				r.get(POL_SORT_ORDER) != null ? r.get(POL_SORT_ORDER) : 0,
				r.get(POL_PR_LINE_ID)
			));

		return Optional.of(toResponse(headerRecord, lines));
	}

	private PurchaseOrderResponse toResponse(Record r, List<PurchaseOrderLineResponse> lines) {
		return new PurchaseOrderResponse(
			r.get(PO_ID),
			r.get(PO_ORDER_NUMBER),
			r.get(PO_ORDER_NUMBER), // name (useCrudPage 호환)
			r.get(PO_ORDER_DATE),
			r.get(PO_PARTNER_ID),
			r.get(DSL.field("partner_name", String.class)),
			r.get(PO_EXPECTED_ARRIVAL),
			r.get(PO_STATUS_CODE),
			r.get(PO_PR_ID),
			r.get(PO_REMARKS) != null ? r.get(PO_REMARKS) : "",
			lines
		);
	}
}
