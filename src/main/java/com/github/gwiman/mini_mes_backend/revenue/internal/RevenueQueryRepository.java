package com.github.gwiman.mini_mes_backend.revenue.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrderLine;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.AvailableOrderLineResponse;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueLineResponse;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueResponse;

import lombok.RequiredArgsConstructor;

/**
 * 매출 읽기 전용 쿼리 리포지토리.
 * revenue/revenue_line 테이블은 jOOQ 코드 생성 대상에 포함되지 않아 원시 DSL로 처리한다.
 * 추후 {@code ./gradlew jooqCodegen} 실행 후 생성 클래스로 교체 가능하다.
 */
@Component
@RequiredArgsConstructor
public class RevenueQueryRepository {

	private final DSLContext dsl;

	// 원시 DSL 테이블/필드 정의 — jOOQ 코드 생성 후 제거 예정
	private static final Table<?> RV = DSL.table("revenue");
	private static final Table<?> RVL = DSL.table("revenue_line");

	private static final Field<Long>       RV_ID          = DSL.field("revenue.id", Long.class);
	private static final Field<String>     RV_NUMBER      = DSL.field("revenue.revenue_number", String.class);
	private static final Field<Long>       RV_PARTNER_ID  = DSL.field("revenue.partner_id", Long.class);
	private static final Field<Long>       RV_EMPLOYEE_ID = DSL.field("revenue.employee_id", Long.class);
	private static final Field<LocalDate>  RV_DATE        = DSL.field("revenue.revenue_date", LocalDate.class);
	private static final Field<String>     RV_STATUS      = DSL.field("revenue.status_code", String.class);
	private static final Field<String>     RV_REMARKS     = DSL.field("revenue.remarks", String.class);

	private static final Field<Long>        RVL_ID                  = DSL.field("revenue_line.id", Long.class);
	private static final Field<Long>        RVL_REVENUE_ID          = DSL.field("revenue_line.revenue_id", Long.class);
	private static final Field<Long>        RVL_SALES_ORDER_LINE_ID = DSL.field("revenue_line.sales_order_line_id", Long.class);
	private static final Field<Long>        RVL_SALES_ORDER_ID      = DSL.field("revenue_line.sales_order_id", Long.class);
	private static final Field<Long>        RVL_ITEM_ID             = DSL.field("revenue_line.item_id", Long.class);
	private static final Field<BigDecimal>  RVL_QUANTITY            = DSL.field("revenue_line.quantity", BigDecimal.class);
	private static final Field<BigDecimal>  RVL_UNIT_PRICE          = DSL.field("revenue_line.unit_price", BigDecimal.class);
	private static final Field<BigDecimal>  RVL_AMOUNT              = DSL.field("revenue_line.amount", BigDecimal.class);
	private static final Field<String>      RVL_REMARKS             = DSL.field("revenue_line.remarks", String.class);
	private static final Field<Integer>     RVL_SORT_ORDER          = DSL.field("revenue_line.sort_order", Integer.class);

	public List<RevenueResponse> search(String statusCode, Long partnerId,
		LocalDate fromDate, LocalDate toDate) {
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;

		Field<String> pName = p.NAME.as("partner_name");
		Field<String> eName = e.NAME.as("employee_name");

		// 목록용 총금액 서브쿼리
		Field<BigDecimal> totalAmount = DSL.field(
			DSL.select(DSL.coalesce(DSL.sum(DSL.field("rvl.amount", BigDecimal.class)), BigDecimal.ZERO))
				.from(DSL.table("revenue_line").as("rvl"))
				.where(DSL.field("rvl.revenue_id", Long.class).eq(RV_ID))
		).as("total_amount");

		Condition statusCond  = statusCode != null ? RV_STATUS.eq(statusCode)           : DSL.noCondition();
		Condition partnerCond = partnerId  != null ? RV_PARTNER_ID.eq(partnerId)        : DSL.noCondition();
		Condition fromCond    = fromDate   != null ? RV_DATE.greaterOrEqual(fromDate)   : DSL.noCondition();
		Condition toCond      = toDate     != null ? RV_DATE.lessOrEqual(toDate)        : DSL.noCondition();

		return dsl
			.select(RV_ID, RV_NUMBER, RV_PARTNER_ID, RV_EMPLOYEE_ID,
				RV_DATE, RV_STATUS, RV_REMARKS, pName, eName, totalAmount)
			.from(RV)
			.leftJoin(p).on(RV_PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(RV_EMPLOYEE_ID.eq(e.ID))
			.where(statusCond).and(partnerCond).and(fromCond).and(toCond)
			.orderBy(RV_ID.desc())
			.fetch()
			.map(r -> toResponse(r, Collections.emptyList()));
	}

	public Optional<RevenueResponse> findByIdWithLines(Long id) {
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		Item i = Item.ITEM;
		SalesOrder so = SalesOrder.SALES_ORDER;

		Field<String> pName = p.NAME.as("partner_name");
		Field<String> eName = e.NAME.as("employee_name");

		Field<BigDecimal> totalAmount = DSL.field(
			DSL.select(DSL.coalesce(DSL.sum(DSL.field("rvl.amount", BigDecimal.class)), BigDecimal.ZERO))
				.from(DSL.table("revenue_line").as("rvl"))
				.where(DSL.field("rvl.revenue_id", Long.class).eq(RV_ID))
		).as("total_amount");

		var header = dsl
			.select(RV_ID, RV_NUMBER, RV_PARTNER_ID, RV_EMPLOYEE_ID,
				RV_DATE, RV_STATUS, RV_REMARKS, pName, eName, totalAmount)
			.from(RV)
			.leftJoin(p).on(RV_PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(RV_EMPLOYEE_ID.eq(e.ID))
			.where(RV_ID.eq(id))
			.fetchOne();

		if (header == null) {
			return Optional.empty();
		}

		Field<String> iCode      = i.CODE.as("item_code");
		Field<String> iName      = i.NAME.as("item_name");
		Field<String> soNumber   = so.ORDER_NUMBER.as("order_number");

		List<RevenueLineResponse> lines = dsl
			.select(RVL_ID, RVL_SALES_ORDER_LINE_ID, RVL_SALES_ORDER_ID,
				RVL_ITEM_ID, RVL_QUANTITY, RVL_UNIT_PRICE, RVL_AMOUNT,
				RVL_REMARKS, RVL_SORT_ORDER, iCode, iName, soNumber)
			.from(RVL)
			.join(i).on(RVL_ITEM_ID.eq(i.ID))
			.join(so).on(RVL_SALES_ORDER_ID.eq(so.ID))
			.where(RVL_REVENUE_ID.eq(id))
			.orderBy(RVL_SORT_ORDER)
			.fetch()
			.map(r -> new RevenueLineResponse(
				r.get(RVL_ID),
				r.get(RVL_SALES_ORDER_LINE_ID),
				r.get(RVL_SALES_ORDER_ID),
				r.get(DSL.field("order_number", String.class)),
				r.get(RVL_ITEM_ID),
				r.get(iCode),
				r.get(iName),
				r.get(RVL_QUANTITY),
				r.get(RVL_UNIT_PRICE),
				r.get(RVL_AMOUNT),
				r.get(RVL_REMARKS) != null ? r.get(RVL_REMARKS) : "",
				r.get(RVL_SORT_ORDER) != null ? r.get(RVL_SORT_ORDER) : 0
			));

		return Optional.of(toResponse(header, lines));
	}

	/**
	 * 거래처의 완료 수주에서 선택 가능한 수주 라인 목록을 반환한다.
	 * 완료 수주(ORDER_STATUS_04)의 라인만 조회한다.
	 */
	public List<AvailableOrderLineResponse> findAvailableOrderLines(Long partnerId) {
		SalesOrder so = SalesOrder.SALES_ORDER;
		SalesOrderLine sol = SalesOrderLine.SALES_ORDER_LINE;
		Item i = Item.ITEM;

		return dsl
			.select(sol.ID, so.ID, so.ORDER_NUMBER,
				i.ID, i.CODE, i.NAME,
				sol.QUANTITY, sol.UNIT_PRICE)
			.from(sol)
			.join(so).on(sol.SALES_ORDER_ID.eq(so.ID))
			.join(i).on(sol.ITEM_ID.eq(i.ID))
			.where(so.PARTNER_ID.eq(partnerId))
			.and(so.STATUS_CODE.eq("ORDER_STATUS_04"))
			.orderBy(so.ORDER_DATE.desc(), sol.SORT_ORDER)
			.fetch()
			.map(r -> new AvailableOrderLineResponse(
				r.get(sol.ID),
				r.get(so.ID),
				r.get(so.ORDER_NUMBER),
				r.get(i.ID),
				r.get(i.CODE),
				r.get(i.NAME),
				r.get(sol.QUANTITY),
				r.get(sol.UNIT_PRICE)
			));
	}

	private RevenueResponse toResponse(Record r, List<RevenueLineResponse> lines) {
		return new RevenueResponse(
			r.get(RV_ID),
			r.get(RV_NUMBER),
			r.get(RV_NUMBER), // name — useCrudPage 호환용
			r.get(RV_PARTNER_ID),
			r.get(DSL.field("partner_name", String.class)),
			r.get(RV_EMPLOYEE_ID),
			r.get(DSL.field("employee_name", String.class)),
			r.get(RV_DATE),
			r.get(RV_STATUS),
			r.get(DSL.field("total_amount", BigDecimal.class)),
			r.get(RV_REMARKS) != null ? r.get(RV_REMARKS) : "",
			lines
		);
	}
}
