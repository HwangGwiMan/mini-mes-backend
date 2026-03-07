package com.github.gwiman.mini_mes_backend.quote.internal;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.Quote;
import com.github.gwiman.mini_mes_backend.jooq.tables.QuoteLine;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteLineResponse;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuoteQueryRepository {

	private static final Field<Long> APPROVER_ID_FIELD =
		DSL.field(DSL.name("quote", "approver_id"), SQLDataType.BIGINT);

	private final DSLContext dsl;

	public List<QuoteResponse> search(String quoteNumberPattern, Long partnerId,
		String statusCode, LocalDate fromDate, LocalDate toDate) {
		Quote q = Quote.QUOTE;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		Employee approver = Employee.EMPLOYEE.as("approver");

		Condition quoteNumberCond = quoteNumberPattern != null
			? q.QUOTE_NUMBER.like(quoteNumberPattern)
			: DSL.noCondition();
		Condition partnerCond = partnerId != null ? q.PARTNER_ID.eq(partnerId) : DSL.noCondition();
		Condition statusCond = statusCode != null ? q.STATUS_CODE.eq(statusCode) : DSL.noCondition();
		Condition fromCond = fromDate != null ? q.QUOTE_DATE.greaterOrEqual(fromDate) : DSL.noCondition();
		Condition toCond = toDate != null ? q.QUOTE_DATE.lessOrEqual(toDate) : DSL.noCondition();

		return dsl
			.select(
				q.ID, q.QUOTE_NUMBER, q.QUOTE_DATE, q.VALID_UNTIL, q.STATUS_CODE, q.REMARKS,
				q.PARTNER_ID, q.EMPLOYEE_ID,
				p.CODE, p.NAME,
				e.CODE, e.NAME,
				APPROVER_ID_FIELD.as("approverId"),
				approver.CODE.as("approverCode"),
				approver.NAME.as("approverName")
			)
			.from(q)
			.leftJoin(p).on(q.PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(q.EMPLOYEE_ID.eq(e.ID))
			.leftJoin(approver).on(APPROVER_ID_FIELD.eq(approver.ID))
			.where(quoteNumberCond)
			.and(partnerCond)
			.and(statusCond)
			.and(fromCond)
			.and(toCond)
			.orderBy(q.QUOTE_DATE.desc(), q.QUOTE_NUMBER.desc())
			.fetch()
			.map(r -> QuoteResponse.fromRecord(r, Collections.emptyList()));
	}

	public Optional<QuoteResponse> findByIdWithLines(Long id) {
		Quote q = Quote.QUOTE;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		Employee approver = Employee.EMPLOYEE.as("approver");

		// Query 1: Quote + Partner + Employee + Approver
		var quoteRecord = dsl
			.select(
				q.ID, q.QUOTE_NUMBER, q.QUOTE_DATE, q.VALID_UNTIL, q.STATUS_CODE, q.REMARKS,
				q.PARTNER_ID, q.EMPLOYEE_ID,
				p.CODE, p.NAME,
				e.CODE, e.NAME,
				APPROVER_ID_FIELD.as("approverId"),
				approver.CODE.as("approverCode"),
				approver.NAME.as("approverName")
			)
			.from(q)
			.leftJoin(p).on(q.PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(q.EMPLOYEE_ID.eq(e.ID))
			.leftJoin(approver).on(APPROVER_ID_FIELD.eq(approver.ID))
			.where(q.ID.eq(id))
			.fetchOne();

		if (quoteRecord == null) {
			return Optional.empty();
		}

		// Query 2: QuoteLine + Item
		QuoteLine ql = QuoteLine.QUOTE_LINE;
		Item i = Item.ITEM;
		List<QuoteLineResponse> lines = dsl
			.select(
				ql.ID, ql.ITEM_ID, ql.QUANTITY, ql.UNIT_PRICE, ql.AMOUNT,
				ql.DELIVERY_REQUEST_DATE, ql.REMARKS, ql.SORT_ORDER,
				i.CODE, i.NAME
			)
			.from(ql)
			.join(i).on(ql.ITEM_ID.eq(i.ID))
			.where(ql.QUOTE_ID.eq(id))
			.orderBy(ql.SORT_ORDER)
			.fetch()
			.map(QuoteLineResponse::fromRecord);

		return Optional.of(QuoteResponse.fromRecord(quoteRecord, lines));
	}

	public Optional<String> findMaxQuoteNumberByPrefix(String prefixPattern) {
		Quote q = Quote.QUOTE;
		return dsl
			.select(DSL.max(q.QUOTE_NUMBER))
			.from(q)
			.where(q.QUOTE_NUMBER.like(prefixPattern))
			.fetchOptional()
			.flatMap(r -> Optional.ofNullable(r.value1()));
	}
}
