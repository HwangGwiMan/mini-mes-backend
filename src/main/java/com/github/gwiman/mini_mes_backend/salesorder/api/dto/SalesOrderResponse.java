package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.time.LocalDate;
import java.util.List;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.Quote;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;

public record SalesOrderResponse(
	Long id,
	String orderNumber,
	String name, // useCrudPage 호환용 (orderNumber와 동일)
	LocalDate orderDate,
	LocalDate deliveryDate,
	Long partnerId,
	String partnerCode,
	String partnerName,
	Long employeeId,
	String employeeCode,
	String employeeName,
	Long quoteId,
	String quoteNumber,
	String statusCode,
	String remarks,
	List<SalesOrderLineResponse> lines
) {
	public static SalesOrderResponse fromRecord(Record r, List<SalesOrderLineResponse> lines) {
		SalesOrder so = SalesOrder.SALES_ORDER;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		Quote q = Quote.QUOTE;
		return new SalesOrderResponse(
			r.get(so.ID),
			r.get(so.ORDER_NUMBER),
			r.get(so.ORDER_NUMBER),
			r.get(so.ORDER_DATE),
			r.get(so.DELIVERY_DATE),
			r.get(so.PARTNER_ID),
			r.get(p.CODE),
			r.get(p.NAME),
			r.get(so.EMPLOYEE_ID),
			r.get(e.CODE),
			r.get(e.NAME),
			r.get(so.QUOTE_ID),
			r.get(q.QUOTE_NUMBER),
			r.get(so.STATUS_CODE),
			r.get(so.REMARKS),
			lines != null ? lines : List.of()
		);
	}
}
