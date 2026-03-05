package com.github.gwiman.mini_mes_backend.salesorder.api.dto;

import java.time.LocalDate;
import java.util.List;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.jooq.tables.Quote;
import com.github.gwiman.mini_mes_backend.jooq.tables.SalesOrder;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SalesOrderResponse {

	private Long id;
	private String orderNumber;
	private String name; // useCrudPage 호환용 (orderNumber와 동일)
	private LocalDate orderDate;
	private LocalDate deliveryDate;
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	private Long employeeId;
	private String employeeCode;
	private String employeeName;
	private Long quoteId;
	private String quoteNumber;
	private String statusCode;
	private String remarks;
	private List<SalesOrderLineResponse> lines;

	public SalesOrderResponse(Long id, String orderNumber, String name,
		LocalDate orderDate, LocalDate deliveryDate,
		Long partnerId, String partnerCode, String partnerName,
		Long employeeId, String employeeCode, String employeeName,
		Long quoteId, String quoteNumber,
		String statusCode, String remarks, List<SalesOrderLineResponse> lines) {
		this.id = id;
		this.orderNumber = orderNumber;
		this.name = name;
		this.orderDate = orderDate;
		this.deliveryDate = deliveryDate;
		this.partnerId = partnerId;
		this.partnerCode = partnerCode;
		this.partnerName = partnerName;
		this.employeeId = employeeId;
		this.employeeCode = employeeCode;
		this.employeeName = employeeName;
		this.quoteId = quoteId;
		this.quoteNumber = quoteNumber;
		this.statusCode = statusCode;
		this.remarks = remarks;
		this.lines = lines;
	}

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
