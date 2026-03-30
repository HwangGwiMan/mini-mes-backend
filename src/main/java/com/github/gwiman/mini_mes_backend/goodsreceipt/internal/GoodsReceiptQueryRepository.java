package com.github.gwiman.mini_mes_backend.goodsreceipt.internal;

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

import com.github.gwiman.mini_mes_backend.goodsreceipt.api.GoodsReceiptLineResponse;
import com.github.gwiman.mini_mes_backend.goodsreceipt.api.GoodsReceiptResponse;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;

import lombok.RequiredArgsConstructor;

/**
 * 자재 입고 읽기 전용 쿼리 리포지토리.
 * goods_receipt/goods_receipt_line 테이블은 jOOQ 코드 생성 대상에 포함되지 않아 원시 DSL로 처리한다.
 * Partner, Item은 기존 jOOQ 생성 클래스를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class GoodsReceiptQueryRepository {

	private final DSLContext dsl;

	// 원시 DSL 테이블/필드 정의 — jOOQ 코드 생성 후 제거 예정
	private static final Table<?> GR  = DSL.table("goods_receipt");
	private static final Table<?> GRL = DSL.table("goods_receipt_line");
	private static final Table<?> PO  = DSL.table("purchase_order");

	private static final Field<Long>      GR_ID             = DSL.field("goods_receipt.id",             Long.class);
	private static final Field<String>    GR_RECEIPT_NUMBER = DSL.field("goods_receipt.receipt_number",  String.class);
	private static final Field<LocalDate> GR_RECEIPT_DATE   = DSL.field("goods_receipt.receipt_date",    LocalDate.class);
	private static final Field<Long>      GR_PO_ID          = DSL.field("goods_receipt.po_id",           Long.class);
	private static final Field<Long>      GR_PARTNER_ID     = DSL.field("goods_receipt.partner_id",      Long.class);
	private static final Field<String>    GR_STATUS_CODE    = DSL.field("goods_receipt.status_code",     String.class);
	private static final Field<String>    GR_REMARKS        = DSL.field("goods_receipt.remarks",         String.class);

	private static final Field<Long>       GRL_ID               = DSL.field("goods_receipt_line.id",                Long.class);
	private static final Field<Long>       GRL_GR_ID            = DSL.field("goods_receipt_line.goods_receipt_id",  Long.class);
	private static final Field<Long>       GRL_ITEM_ID          = DSL.field("goods_receipt_line.item_id",           Long.class);
	private static final Field<Long>       GRL_PO_LINE_ID       = DSL.field("goods_receipt_line.po_line_id",        Long.class);
	private static final Field<String>     GRL_RECEIPT_TYPE     = DSL.field("goods_receipt_line.receipt_type_code", String.class);
	private static final Field<BigDecimal> GRL_RECV_QTY         = DSL.field("goods_receipt_line.received_quantity", BigDecimal.class);
	private static final Field<BigDecimal> GRL_UNIT_PRICE       = DSL.field("goods_receipt_line.unit_price",        BigDecimal.class);
	private static final Field<String>     GRL_REMARKS          = DSL.field("goods_receipt_line.remarks",           String.class);
	private static final Field<Integer>    GRL_SORT_ORDER       = DSL.field("goods_receipt_line.sort_order",        Integer.class);

	private static final Field<String> PO_ORDER_NUMBER = DSL.field("purchase_order.order_number", String.class);

	public List<GoodsReceiptResponse> search(String receiptNumberPattern,
			String partnerNamePattern, String statusCode) {
		Partner p = Partner.PARTNER;
		Field<String> partnerCode = p.CODE.as("partner_code");
		Field<String> partnerName = p.NAME.as("partner_name");
		Field<String> poNumber    = PO_ORDER_NUMBER.as("po_number");

		Condition numberCond  = receiptNumberPattern != null ? GR_RECEIPT_NUMBER.like(receiptNumberPattern)  : DSL.noCondition();
		Condition partnerCond = partnerNamePattern   != null ? p.NAME.likeIgnoreCase(partnerNamePattern)     : DSL.noCondition();
		Condition statusCond  = statusCode           != null ? GR_STATUS_CODE.eq(statusCode)                 : DSL.noCondition();

		return dsl
			.select(GR_ID, GR_RECEIPT_NUMBER, GR_RECEIPT_DATE, GR_PO_ID,
					GR_PARTNER_ID, GR_STATUS_CODE, GR_REMARKS,
					partnerCode, partnerName, poNumber)
			.from(GR)
			.leftJoin(p).on(GR_PARTNER_ID.eq(p.ID))
			.leftJoin(PO).on(GR_PO_ID.eq(DSL.field("purchase_order.id", Long.class)))
			.where(numberCond).and(partnerCond).and(statusCond)
			.orderBy(GR_ID.desc())
			.fetch()
			.map(r -> toResponse(r, List.of()));
	}

	public Optional<GoodsReceiptResponse> findByIdWithLines(Long id) {
		Partner p    = Partner.PARTNER;
		Item    item = Item.ITEM;
		Field<String> partnerCode = p.CODE.as("partner_code");
		Field<String> partnerName = p.NAME.as("partner_name");
		Field<String> poNumber    = PO_ORDER_NUMBER.as("po_number");

		// Query 1: 헤더 + 거래처 + 발주번호
		var headerRecord = dsl
			.select(GR_ID, GR_RECEIPT_NUMBER, GR_RECEIPT_DATE, GR_PO_ID,
					GR_PARTNER_ID, GR_STATUS_CODE, GR_REMARKS,
					partnerCode, partnerName, poNumber)
			.from(GR)
			.leftJoin(p).on(GR_PARTNER_ID.eq(p.ID))
			.leftJoin(PO).on(GR_PO_ID.eq(DSL.field("purchase_order.id", Long.class)))
			.where(GR_ID.eq(id))
			.fetchOne();

		if (headerRecord == null) {
			return Optional.empty();
		}

		// Query 2: 라인 + 품목 코드/명
		Field<String> itemCode = item.CODE.as("item_code");
		Field<String> itemName = item.NAME.as("item_name");

		List<GoodsReceiptLineResponse> lines = dsl
			.select(GRL_ID, GRL_ITEM_ID, GRL_PO_LINE_ID, GRL_RECEIPT_TYPE,
					GRL_RECV_QTY, GRL_UNIT_PRICE, GRL_REMARKS, GRL_SORT_ORDER,
					itemCode, itemName)
			.from(GRL)
			.join(item).on(GRL_ITEM_ID.eq(item.ID))
			.where(GRL_GR_ID.eq(id))
			.orderBy(GRL_SORT_ORDER)
			.fetch()
			.map(r -> new GoodsReceiptLineResponse(
				r.get(GRL_ID),
				r.get(GRL_ITEM_ID),
				r.get(itemCode),
				r.get(itemName),
				r.get(GRL_PO_LINE_ID),
				r.get(GRL_RECEIPT_TYPE),
				r.get(GRL_RECV_QTY),
				r.get(GRL_UNIT_PRICE),
				r.get(GRL_REMARKS) != null ? r.get(GRL_REMARKS) : "",
				r.get(GRL_SORT_ORDER) != null ? r.get(GRL_SORT_ORDER) : 0
			));

		return Optional.of(toResponse(headerRecord, lines));
	}

	private GoodsReceiptResponse toResponse(Record r, List<GoodsReceiptLineResponse> lines) {
		return new GoodsReceiptResponse(
			r.get(GR_ID),
			r.get(GR_RECEIPT_NUMBER),
			r.get(GR_RECEIPT_NUMBER), // name (useCrudPage 호환)
			r.get(GR_RECEIPT_DATE),
			r.get(GR_PO_ID),
			r.get(DSL.field("po_number", String.class)),
			r.get(GR_PARTNER_ID),
			r.get(DSL.field("partner_code", String.class)),
			r.get(DSL.field("partner_name", String.class)),
			r.get(GR_STATUS_CODE),
			r.get(GR_REMARKS) != null ? r.get(GR_REMARKS) : "",
			lines
		);
	}
}
