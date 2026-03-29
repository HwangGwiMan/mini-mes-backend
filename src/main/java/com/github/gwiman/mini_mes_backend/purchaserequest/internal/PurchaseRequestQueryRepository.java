package com.github.gwiman.mini_mes_backend.purchaserequest.internal;

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

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestLineResponse;
import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestResponse;

import lombok.RequiredArgsConstructor;

/**
 * 구매 요청 읽기 전용 쿼리 리포지토리.
 * purchase_request/purchase_request_line 테이블은 jOOQ 코드 생성 대상에 아직 포함되지 않아 원시 DSL로 처리한다.
 * 추후 {@code ./gradlew jooqCodegen} 실행 후 생성 클래스로 교체 가능하다.
 */
@Component
@RequiredArgsConstructor
public class PurchaseRequestQueryRepository {

	private final DSLContext dsl;

	// 원시 DSL 테이블/필드 정의 — jOOQ 코드 생성 후 제거 예정
	private static final Table<?> PR    = DSL.table("purchase_request");
	private static final Table<?> PRL   = DSL.table("purchase_request_line");

	private static final Field<Long>      PR_ID           = DSL.field("purchase_request.id",             Long.class);
	private static final Field<String>    PR_NUMBER       = DSL.field("purchase_request.request_number", String.class);
	private static final Field<LocalDate> PR_DATE         = DSL.field("purchase_request.request_date",   LocalDate.class);
	private static final Field<Long>      PR_REQUESTER_ID = DSL.field("purchase_request.requester_id",   Long.class);
	private static final Field<String>    PR_STATUS_CODE  = DSL.field("purchase_request.status_code",    String.class);
	private static final Field<String>    PR_REMARKS      = DSL.field("purchase_request.remarks",        String.class);

	private static final Field<Long>           PRL_ID           = DSL.field("purchase_request_line.id",               Long.class);
	private static final Field<Long>           PRL_PR_ID        = DSL.field("purchase_request_line.purchase_request_id", Long.class);
	private static final Field<Long>           PRL_ITEM_ID      = DSL.field("purchase_request_line.item_id",           Long.class);
	private static final Field<java.math.BigDecimal> PRL_QTY    = DSL.field("purchase_request_line.requested_quantity", java.math.BigDecimal.class);
	private static final Field<LocalDate>      PRL_REQUIRED_DATE= DSL.field("purchase_request_line.required_date",     LocalDate.class);
	private static final Field<String>         PRL_REMARKS      = DSL.field("purchase_request_line.remarks",           String.class);
	private static final Field<Integer>        PRL_SORT_ORDER   = DSL.field("purchase_request_line.sort_order",        Integer.class);

	public List<PurchaseRequestResponse> search(String requestNumberPattern, Long requesterId,
			String statusCode, LocalDate fromDate, LocalDate toDate) {
		Employee emp = Employee.EMPLOYEE;
		Field<String> empName = emp.NAME.as("requester_name");

		Condition numberCond  = requestNumberPattern != null ? PR_NUMBER.like(requestNumberPattern) : DSL.noCondition();
		Condition requesterCond = requesterId != null ? PR_REQUESTER_ID.eq(requesterId) : DSL.noCondition();
		Condition statusCond  = statusCode != null ? PR_STATUS_CODE.eq(statusCode) : DSL.noCondition();
		Condition fromCond    = fromDate != null ? PR_DATE.greaterOrEqual(fromDate) : DSL.noCondition();
		Condition toCond      = toDate   != null ? PR_DATE.lessOrEqual(toDate)     : DSL.noCondition();

		return dsl
			.select(PR_ID, PR_NUMBER, PR_DATE, PR_REQUESTER_ID, PR_STATUS_CODE, PR_REMARKS, empName)
			.from(PR)
			.leftJoin(emp).on(PR_REQUESTER_ID.eq(emp.ID))
			.where(numberCond).and(requesterCond).and(statusCond).and(fromCond).and(toCond)
			.orderBy(PR_ID.desc())
			.fetch()
			.map(r -> toResponse(r, List.of()));
	}

	public Optional<PurchaseRequestResponse> findByIdWithLines(Long id) {
		Employee emp = Employee.EMPLOYEE;
		Item item = Item.ITEM;
		Field<String> empName = emp.NAME.as("requester_name");

		// Query 1: 헤더 + 요청자 이름
		var headerRecord = dsl
			.select(PR_ID, PR_NUMBER, PR_DATE, PR_REQUESTER_ID, PR_STATUS_CODE, PR_REMARKS, empName)
			.from(PR)
			.leftJoin(emp).on(PR_REQUESTER_ID.eq(emp.ID))
			.where(PR_ID.eq(id))
			.fetchOne();

		if (headerRecord == null) {
			return Optional.empty();
		}

		// Query 2: 라인 + 품목 코드/명
		Field<String> itemCode = item.CODE.as("item_code");
		Field<String> itemName = item.NAME.as("item_name");

		List<PurchaseRequestLineResponse> lines = dsl
			.select(PRL_ID, PRL_ITEM_ID, PRL_QTY, PRL_REQUIRED_DATE, PRL_REMARKS, PRL_SORT_ORDER, itemCode, itemName)
			.from(PRL)
			.join(item).on(PRL_ITEM_ID.eq(item.ID))
			.where(PRL_PR_ID.eq(id))
			.orderBy(PRL_SORT_ORDER)
			.fetch()
			.map(r -> new PurchaseRequestLineResponse(
				r.get(PRL_ID),
				r.get(PRL_ITEM_ID),
				r.get(itemCode),
				r.get(itemName),
				r.get(PRL_QTY),
				r.get(PRL_REQUIRED_DATE),
				r.get(PRL_REMARKS) != null ? r.get(PRL_REMARKS) : "",
				r.get(PRL_SORT_ORDER) != null ? r.get(PRL_SORT_ORDER) : 0
			));

		return Optional.of(toResponse(headerRecord, lines));
	}

	private PurchaseRequestResponse toResponse(Record r, List<PurchaseRequestLineResponse> lines) {
		return new PurchaseRequestResponse(
			r.get(PR_ID),
			r.get(PR_NUMBER),
			r.get(PR_NUMBER), // name 필드 (useCrudPage 호환)
			r.get(PR_DATE),
			r.get(PR_REQUESTER_ID),
			r.get(DSL.field("requester_name", String.class)),
			r.get(PR_STATUS_CODE),
			r.get(PR_REMARKS) != null ? r.get(PR_REMARKS) : "",
			lines
		);
	}
}
