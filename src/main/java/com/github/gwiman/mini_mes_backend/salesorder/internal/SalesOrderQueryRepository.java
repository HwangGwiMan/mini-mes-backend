package com.github.gwiman.mini_mes_backend.salesorder.internal;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.Quote;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrderLine;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderLineResponse;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SalesOrderQueryRepository {

	private final DSLContext dsl;

	public List<SalesOrderResponse> search(String orderNumberPattern, Long partnerId,
		String statusCode, LocalDate fromDate, LocalDate toDate) {
		SalesOrder so = SalesOrder.SALES_ORDER;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		Quote q = Quote.QUOTE;

		Condition orderNumberCond = orderNumberPattern != null
			? so.ORDER_NUMBER.like(orderNumberPattern)
			: DSL.noCondition();
		Condition partnerCond = partnerId != null ? so.PARTNER_ID.eq(partnerId) : DSL.noCondition();
		Condition statusCond = statusCode != null ? so.STATUS_CODE.eq(statusCode) : DSL.noCondition();
		Condition fromCond = fromDate != null ? so.ORDER_DATE.greaterOrEqual(fromDate) : DSL.noCondition();
		Condition toCond = toDate != null ? so.ORDER_DATE.lessOrEqual(toDate) : DSL.noCondition();

		return dsl
			.select(
				so.ID, so.ORDER_NUMBER, so.ORDER_DATE, so.DELIVERY_DATE, so.STATUS_CODE, so.REMARKS,
				so.PARTNER_ID, so.EMPLOYEE_ID, so.QUOTE_ID,
				p.CODE, p.NAME,
				e.CODE, e.NAME,
				q.QUOTE_NUMBER
			)
			.from(so)
			.leftJoin(p).on(so.PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(so.EMPLOYEE_ID.eq(e.ID))
			.leftJoin(q).on(so.QUOTE_ID.eq(q.ID))
			.where(orderNumberCond)
			.and(partnerCond)
			.and(statusCond)
			.and(fromCond)
			.and(toCond)
			.orderBy(so.ORDER_DATE.desc(), so.ORDER_NUMBER.desc())
			.fetch()
			.map(r -> SalesOrderResponse.fromRecord(r, Collections.emptyList()));
	}

	public Optional<SalesOrderResponse> findByIdWithLines(Long id) {
		SalesOrder so = SalesOrder.SALES_ORDER;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		Quote q = Quote.QUOTE;

		var orderRecord = dsl
			.select(
				so.ID, so.ORDER_NUMBER, so.ORDER_DATE, so.DELIVERY_DATE, so.STATUS_CODE, so.REMARKS,
				so.PARTNER_ID, so.EMPLOYEE_ID, so.QUOTE_ID,
				p.CODE, p.NAME,
				e.CODE, e.NAME,
				q.QUOTE_NUMBER
			)
			.from(so)
			.leftJoin(p).on(so.PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(so.EMPLOYEE_ID.eq(e.ID))
			.leftJoin(q).on(so.QUOTE_ID.eq(q.ID))
			.where(so.ID.eq(id))
			.fetchOne();

		if (orderRecord == null) {
			return Optional.empty();
		}

		SalesOrderLine sol = SalesOrderLine.SALES_ORDER_LINE;
		Item i = Item.ITEM;
		List<SalesOrderLineResponse> lines = dsl
			.select(
				sol.ID, sol.ITEM_ID, sol.QUANTITY, sol.UNIT_PRICE, sol.AMOUNT,
				sol.DELIVERY_REQUEST_DATE, sol.REMARKS, sol.SORT_ORDER,
				i.CODE, i.NAME
			)
			.from(sol)
			.join(i).on(sol.ITEM_ID.eq(i.ID))
			.where(sol.SALES_ORDER_ID.eq(id))
			.orderBy(sol.SORT_ORDER)
			.fetch()
			.map(SalesOrderLineResponse::fromRecord);

		return Optional.of(SalesOrderResponse.fromRecord(orderRecord, lines));
	}

}
