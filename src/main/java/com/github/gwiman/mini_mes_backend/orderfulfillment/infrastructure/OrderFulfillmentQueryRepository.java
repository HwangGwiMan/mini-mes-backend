package com.github.gwiman.mini_mes_backend.orderfulfillment.infrastructure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;
import com.github.gwiman.mini_mes_backend.orderfulfillment.api.dto.OrderFulfillmentResponse;

import lombok.RequiredArgsConstructor;

/**
 * 수주이행현황 읽기 전용 쿼리 리포지토리.
 * 수주·출하·매출 테이블을 한 번의 쿼리로 집계하여 N+1 문제를 방지한다.
 * shipment·revenue 테이블은 jOOQ 코드 생성 대상에 포함되지 않아 원시 DSL로 처리한다.
 */
@Component
@RequiredArgsConstructor
public class OrderFulfillmentQueryRepository {

	private final DSLContext dsl;

	// 원시 DSL 테이블/필드 — jOOQ 코드 생성 후 제거 예정
	private static final Table<?> SH = DSL.table("shipment");
	private static final Field<Long> SH_ID = DSL.field("shipment.id", Long.class);
	private static final Field<String> SH_NUMBER = DSL.field("shipment.shipment_number", String.class);
	private static final Field<Long> SH_SALES_ORDER_ID = DSL.field("shipment.sales_order_id", Long.class);
	private static final Field<LocalDate> SH_SHIPMENT_DATE = DSL.field("shipment.shipment_date", LocalDate.class);
	private static final Field<String> SH_STATUS_CODE = DSL.field("shipment.status_code", String.class);

	public List<OrderFulfillmentResponse> search(
		String orderNumberPattern, Long partnerId,
		String orderStatusCode, String shipmentStatusCode,
		LocalDate fromDate, LocalDate toDate) {

		SalesOrder so = SalesOrder.SALES_ORDER;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;

		Field<String> pName = p.NAME.as("partner_name");
		Field<String> eName = e.NAME.as("employee_name");

		// 수주 총금액 서브쿼리
		Field<BigDecimal> totalOrderAmount = DSL.field(
			DSL.select(DSL.coalesce(DSL.sum(DSL.field("sol.amount", BigDecimal.class)), BigDecimal.ZERO))
				.from(DSL.table("sales_order_line").as("sol"))
				.where(DSL.field("sol.sales_order_id", Long.class).eq(so.ID))
		).as("total_order_amount");

		// 출하 계획 금액 서브쿼리
		Field<BigDecimal> totalPlannedAmount = DSL.field(
			DSL.select(DSL.coalesce(DSL.sum(DSL.field("shl.planned_amount", BigDecimal.class)), BigDecimal.ZERO))
				.from(DSL.table("shipment_line").as("shl"))
				.where(DSL.field("shl.shipment_id", Long.class).eq(SH_ID))
		).as("total_planned_amount");

		// 출하 실적 금액 서브쿼리 (완료 시에만 값 존재)
		Field<BigDecimal> totalActualAmount = DSL.field(
			DSL.select(DSL.sum(DSL.field("shl2.actual_amount", BigDecimal.class)))
				.from(DSL.table("shipment_line").as("shl2"))
				.where(DSL.field("shl2.shipment_id", Long.class).eq(SH_ID))
		).as("total_actual_amount");

		// 마감 매출 합계 서브쿼리 (REVENUE_STATUS_02만 포함)
		Field<BigDecimal> totalRevenueAmount = DSL.field(
			DSL.select(DSL.coalesce(DSL.sum(DSL.field("rvl.amount", BigDecimal.class)), BigDecimal.ZERO))
				.from(DSL.table("revenue_line").as("rvl"))
				.join(DSL.table("revenue").as("rv")).on(
					DSL.field("rvl.revenue_id", Long.class).eq(DSL.field("rv.id", Long.class)))
				.where(DSL.field("rvl.sales_order_id", Long.class).eq(so.ID))
				.and(DSL.field("rv.status_code", String.class).eq("REVENUE_STATUS_02"))
		).as("total_revenue_amount");

		// 매출 상태 요약 서브쿼리 — 연결 매출 상태의 distinct count로 혼재 여부 판단
		// 단일 상태면 해당 라벨, 여러 상태면 "혼재", 없으면 "없음"
		Field<Integer> revenueDraftCount = DSL.field(
			DSL.select(DSL.count())
				.from(DSL.table("revenue_line").as("rvl2"))
				.join(DSL.table("revenue").as("rv2")).on(
					DSL.field("rvl2.revenue_id", Long.class).eq(DSL.field("rv2.id", Long.class)))
				.where(DSL.field("rvl2.sales_order_id", Long.class).eq(so.ID))
				.and(DSL.field("rv2.status_code", String.class).eq("REVENUE_STATUS_01"))
		).as("revenue_draft_count");

		Field<Integer> revenueClosedCount = DSL.field(
			DSL.select(DSL.count())
				.from(DSL.table("revenue_line").as("rvl3"))
				.join(DSL.table("revenue").as("rv3")).on(
					DSL.field("rvl3.revenue_id", Long.class).eq(DSL.field("rv3.id", Long.class)))
				.where(DSL.field("rvl3.sales_order_id", Long.class).eq(so.ID))
				.and(DSL.field("rv3.status_code", String.class).eq("REVENUE_STATUS_02"))
		).as("revenue_closed_count");

		Condition orderNumberCond = orderNumberPattern != null
			? so.ORDER_NUMBER.like(orderNumberPattern) : DSL.noCondition();
		Condition partnerCond = partnerId != null ? so.PARTNER_ID.eq(partnerId) : DSL.noCondition();
		Condition orderStatusCond = orderStatusCode != null ? so.STATUS_CODE.eq(orderStatusCode) : DSL.noCondition();
		Condition shipmentStatusCond = shipmentStatusCode != null ? SH_STATUS_CODE.eq(shipmentStatusCode) : DSL.noCondition();
		Condition fromCond = fromDate != null ? so.ORDER_DATE.greaterOrEqual(fromDate) : DSL.noCondition();
		Condition toCond = toDate != null ? so.ORDER_DATE.lessOrEqual(toDate) : DSL.noCondition();

		return dsl
			.select(
				so.ID, so.ORDER_NUMBER, so.ORDER_DATE, so.DELIVERY_DATE, so.STATUS_CODE,
				pName, eName,
				SH_ID, SH_NUMBER, SH_STATUS_CODE, SH_SHIPMENT_DATE,
				totalOrderAmount, totalPlannedAmount, totalActualAmount,
				totalRevenueAmount, revenueDraftCount, revenueClosedCount
			)
			.from(so)
			.leftJoin(p).on(so.PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(so.EMPLOYEE_ID.eq(e.ID))
			.leftJoin(SH).on(SH_SALES_ORDER_ID.eq(so.ID))
			.where(orderNumberCond)
			.and(partnerCond)
			.and(orderStatusCond)
			.and(shipmentStatusCond)
			.and(fromCond)
			.and(toCond)
			.orderBy(so.ORDER_DATE.desc(), so.ID.desc())
			.fetch()
			.map(this::toResponse);
	}

	private OrderFulfillmentResponse toResponse(Record r) {
		SalesOrder so = SalesOrder.SALES_ORDER;

		BigDecimal orderAmount = r.get(DSL.field("total_order_amount", BigDecimal.class));
		BigDecimal actualAmount = r.get(DSL.field("total_actual_amount", BigDecimal.class));
		BigDecimal revenueAmount = r.get(DSL.field("total_revenue_amount", BigDecimal.class));

		// 이행률: 실제 출하금액 / 수주금액 × 100, 수주금액이 0이거나 출하 실적이 없으면 null
		BigDecimal fulfillmentRate = null;
		if (actualAmount != null && orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) > 0) {
			fulfillmentRate = actualAmount
				.multiply(BigDecimal.valueOf(100))
				.divide(orderAmount, 1, RoundingMode.HALF_UP);
		}

		// 매출 상태 요약: 초안/마감 건수 조합으로 결정
		Integer draftCount = r.get(DSL.field("revenue_draft_count", Integer.class));
		Integer closedCount = r.get(DSL.field("revenue_closed_count", Integer.class));
		String revenueStatusSummary = resolveRevenueStatusSummary(draftCount, closedCount);

		return new OrderFulfillmentResponse(
			r.get(so.ID),
			r.get(so.ORDER_NUMBER),
			r.get(so.ORDER_DATE),
			r.get(so.DELIVERY_DATE),
			r.get(DSL.field("partner_name", String.class)),
			r.get(DSL.field("employee_name", String.class)),
			r.get(so.STATUS_CODE),
			r.get(SH_ID),
			r.get(SH_NUMBER),
			r.get(SH_STATUS_CODE),
			r.get(SH_SHIPMENT_DATE),
			r.get(DSL.field("total_planned_amount", BigDecimal.class)),
			actualAmount,
			orderAmount,
			revenueAmount != null ? revenueAmount : BigDecimal.ZERO,
			revenueStatusSummary,
			fulfillmentRate
		);
	}

	/**
	 * 초안·마감 매출 건수 조합으로 매출 상태 요약 문자열을 결정한다.
	 * 두 상태가 모두 존재하면 "혼재", 하나만 존재하면 해당 상태, 없으면 "없음"을 반환한다.
	 */
	private String resolveRevenueStatusSummary(Integer draftCount, Integer closedCount) {
		boolean hasDraft = draftCount != null && draftCount > 0;
		boolean hasClosed = closedCount != null && closedCount > 0;
		if (hasDraft && hasClosed) return "혼재";
		if (hasClosed) return "마감";
		if (hasDraft) return "초안";
		return "없음";
	}
}
