package com.github.gwiman.mini_mes_backend.shipment.internal;

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
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentLineResponse;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentResponse;

import lombok.RequiredArgsConstructor;

/**
 * 출하 읽기 전용 쿼리 리포지토리.
 * shipment/shipment_line 테이블은 jOOQ 코드 생성 대상에 아직 포함되지 않아 원시 DSL로 처리한다.
 * 추후 {@code ./gradlew jooqCodegen} 실행 후 생성 클래스로 교체 가능하다.
 */
@Component
@RequiredArgsConstructor
public class ShipmentQueryRepository {

	private final DSLContext dsl;

	// 원시 DSL 테이블/필드 정의 — jOOQ 코드 생성 후 제거 예정
	private static final Table<?> SH = DSL.table("shipment");
	private static final Table<?> SHL = DSL.table("shipment_line");
	private static final Field<Long> SH_ID = DSL.field("shipment.id", Long.class);
	private static final Field<String> SH_NUMBER = DSL.field("shipment.shipment_number", String.class);
	private static final Field<Long> SH_SALES_ORDER_ID = DSL.field("shipment.sales_order_id", Long.class);
	private static final Field<LocalDate> SH_SHIPMENT_DATE = DSL.field("shipment.shipment_date", LocalDate.class);
	private static final Field<Long> SH_PARTNER_ID = DSL.field("shipment.partner_id", Long.class);
	private static final Field<Long> SH_EMPLOYEE_ID = DSL.field("shipment.employee_id", Long.class);
	private static final Field<String> SH_STATUS_CODE = DSL.field("shipment.status_code", String.class);
	private static final Field<String> SH_REMARKS = DSL.field("shipment.remarks", String.class);

	private static final Field<Long> SHL_ID = DSL.field("shipment_line.id", Long.class);
	private static final Field<Long> SHL_SHIPMENT_ID = DSL.field("shipment_line.shipment_id", Long.class);
	private static final Field<Long> SHL_SALES_ORDER_LINE_ID = DSL.field("shipment_line.sales_order_line_id", Long.class);
	private static final Field<Long> SHL_ITEM_ID = DSL.field("shipment_line.item_id", Long.class);
	private static final Field<java.math.BigDecimal> SHL_PLANNED_QTY = DSL.field("shipment_line.planned_quantity", java.math.BigDecimal.class);
	private static final Field<java.math.BigDecimal> SHL_ACTUAL_QTY = DSL.field("shipment_line.actual_quantity", java.math.BigDecimal.class);
	private static final Field<java.math.BigDecimal> SHL_UNIT_PRICE = DSL.field("shipment_line.unit_price", java.math.BigDecimal.class);
	private static final Field<java.math.BigDecimal> SHL_PLANNED_AMOUNT = DSL.field("shipment_line.planned_amount", java.math.BigDecimal.class);
	private static final Field<java.math.BigDecimal> SHL_ACTUAL_AMOUNT = DSL.field("shipment_line.actual_amount", java.math.BigDecimal.class);
	private static final Field<String> SHL_REMARKS = DSL.field("shipment_line.remarks", String.class);
	private static final Field<Integer> SHL_SORT_ORDER = DSL.field("shipment_line.sort_order", Integer.class);

	public List<ShipmentResponse> search(String statusCode, Long salesOrderId, Long partnerId,
		LocalDate fromDate, LocalDate toDate) {
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		SalesOrder so = SalesOrder.SALES_ORDER;

		// 수주번호 컬럼 별칭: jOOQ 생성 클래스와 원시 DSL 필드 이름 충돌 방지
		Field<String> soOrderNumber = so.ORDER_NUMBER.as("so_order_number");
		Field<String> pName = p.NAME.as("partner_name");
		Field<String> eName = e.NAME.as("employee_name");

		Condition statusCond = statusCode != null ? SH_STATUS_CODE.eq(statusCode) : DSL.noCondition();
		Condition orderCond = salesOrderId != null ? SH_SALES_ORDER_ID.eq(salesOrderId) : DSL.noCondition();
		Condition partnerCond = partnerId != null ? SH_PARTNER_ID.eq(partnerId) : DSL.noCondition();
		Condition fromCond = fromDate != null ? SH_SHIPMENT_DATE.greaterOrEqual(fromDate) : DSL.noCondition();
		Condition toCond = toDate != null ? SH_SHIPMENT_DATE.lessOrEqual(toDate) : DSL.noCondition();

		return dsl
			.select(
				SH_ID, SH_NUMBER, SH_SALES_ORDER_ID, SH_SHIPMENT_DATE,
				SH_PARTNER_ID, SH_EMPLOYEE_ID, SH_STATUS_CODE, SH_REMARKS,
				soOrderNumber, pName, eName
			)
			.from(SH)
			.leftJoin(so).on(SH_SALES_ORDER_ID.eq(so.ID))
			.leftJoin(p).on(SH_PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(SH_EMPLOYEE_ID.eq(e.ID))
			.where(statusCond)
			.and(orderCond)
			.and(partnerCond)
			.and(fromCond)
			.and(toCond)
			.orderBy(SH_ID.desc())
			.fetch()
			.map(r -> toResponse(r, List.of()));
	}

	public Optional<ShipmentResponse> findByIdWithLines(Long id) {
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		SalesOrder so = SalesOrder.SALES_ORDER;
		Item i = Item.ITEM;

		Field<String> soOrderNumber = so.ORDER_NUMBER.as("so_order_number");
		Field<String> pName = p.NAME.as("partner_name");
		Field<String> eName = e.NAME.as("employee_name");

		var headerRecord = dsl
			.select(
				SH_ID, SH_NUMBER, SH_SALES_ORDER_ID, SH_SHIPMENT_DATE,
				SH_PARTNER_ID, SH_EMPLOYEE_ID, SH_STATUS_CODE, SH_REMARKS,
				soOrderNumber, pName, eName
			)
			.from(SH)
			.leftJoin(so).on(SH_SALES_ORDER_ID.eq(so.ID))
			.leftJoin(p).on(SH_PARTNER_ID.eq(p.ID))
			.leftJoin(e).on(SH_EMPLOYEE_ID.eq(e.ID))
			.where(SH_ID.eq(id))
			.fetchOne();

		if (headerRecord == null) {
			return Optional.empty();
		}

		Field<String> iCode = i.CODE.as("item_code");
		Field<String> iName = i.NAME.as("item_name");

		List<ShipmentLineResponse> lines = dsl
			.select(
				SHL_ID, SHL_SHIPMENT_ID, SHL_SALES_ORDER_LINE_ID, SHL_ITEM_ID,
				SHL_PLANNED_QTY, SHL_ACTUAL_QTY, SHL_UNIT_PRICE,
				SHL_PLANNED_AMOUNT, SHL_ACTUAL_AMOUNT, SHL_REMARKS, SHL_SORT_ORDER,
				iCode, iName
			)
			.from(SHL)
			.join(i).on(SHL_ITEM_ID.eq(i.ID))
			.where(SHL_SHIPMENT_ID.eq(id))
			.orderBy(SHL_SORT_ORDER)
			.fetch()
			.map(r -> new ShipmentLineResponse(
				r.get(SHL_ID),
				r.get(SHL_SALES_ORDER_LINE_ID),
				r.get(SHL_ITEM_ID),
				r.get(iCode),
				r.get(iName),
				r.get(SHL_PLANNED_QTY),
				r.get(SHL_ACTUAL_QTY),
				r.get(SHL_UNIT_PRICE),
				r.get(SHL_PLANNED_AMOUNT),
				r.get(SHL_ACTUAL_AMOUNT),
				r.get(SHL_REMARKS) != null ? r.get(SHL_REMARKS) : "",
				r.get(SHL_SORT_ORDER) != null ? r.get(SHL_SORT_ORDER) : 0
			));

		return Optional.of(toResponse(headerRecord, lines));
	}

	private ShipmentResponse toResponse(Record r, List<ShipmentLineResponse> lines) {
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		return new ShipmentResponse(
			r.get(SH_ID),
			r.get(SH_NUMBER),
			r.get(SH_NUMBER), // name 필드 (useCrudPage 호환)
			r.get(SH_SALES_ORDER_ID),
			r.get(DSL.field("so_order_number", String.class)),
			r.get(SH_SHIPMENT_DATE),
			r.get(SH_PARTNER_ID),
			r.get(DSL.field("partner_name", String.class)),
			r.get(SH_EMPLOYEE_ID),
			r.get(DSL.field("employee_name", String.class)),
			r.get(SH_STATUS_CODE),
			r.get(SH_REMARKS) != null ? r.get(SH_REMARKS) : "",
			lines
		);
	}
}
