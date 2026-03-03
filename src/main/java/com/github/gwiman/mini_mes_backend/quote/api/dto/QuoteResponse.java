package com.github.gwiman.mini_mes_backend.quote.api.dto;

import java.time.LocalDate;
import java.util.List;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.jooq.tables.Employee;
import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.quote.domain.Quote;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuoteResponse {

	private Long id;
	private String quoteNumber;
	private String name; // useCrudPage 호환용 (quoteNumber와 동일)
	private LocalDate quoteDate;
	private LocalDate validUntil;
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	private Long employeeId;
	private String employeeCode;
	private String employeeName;
	private String statusCode;
	private String remarks;
	private List<QuoteLineResponse> lines;

	public QuoteResponse(Long id, String quoteNumber, String name, LocalDate quoteDate, LocalDate validUntil,
		Long partnerId, String partnerCode, String partnerName,
		Long employeeId, String employeeCode, String employeeName,
		String statusCode, String remarks, List<QuoteLineResponse> lines) {
		this.id = id;
		this.quoteNumber = quoteNumber;
		this.name = name;
		this.quoteDate = quoteDate;
		this.validUntil = validUntil;
		this.partnerId = partnerId;
		this.partnerCode = partnerCode;
		this.partnerName = partnerName;
		this.employeeId = employeeId;
		this.employeeCode = employeeCode;
		this.employeeName = employeeName;
		this.statusCode = statusCode;
		this.remarks = remarks;
		this.lines = lines;
	}

	public static QuoteResponse from(Quote entity) {
		List<QuoteLineResponse> lineResponses = entity.getLines().stream()
			.map(QuoteLineResponse::from)
			.toList();

		Long empId = entity.getEmployee() != null ? entity.getEmployee().getId() : null;
		String empCode = entity.getEmployee() != null ? entity.getEmployee().getCode() : null;
		String empName = entity.getEmployee() != null ? entity.getEmployee().getName() : null;

		return new QuoteResponse(
			entity.getId(),
			entity.getQuoteNumber(),
			entity.getQuoteNumber(),
			entity.getQuoteDate(),
			entity.getValidUntil(),
			entity.getPartner().getId(),
			entity.getPartner().getCode(),
			entity.getPartner().getName(),
			empId,
			empCode,
			empName,
			entity.getStatusCode(),
			entity.getRemarks(),
			lineResponses
		);
	}

	/**
	 * jOOQ Record를 QuoteResponse로 매핑 (Quote + Partner + Employee 조인 결과용).
	 */
	public static QuoteResponse fromRecord(Record r, List<QuoteLineResponse> lines) {
		com.github.gwiman.mini_mes_backend.jooq.tables.Quote q = com.github.gwiman.mini_mes_backend.jooq.tables.Quote.QUOTE;
		Partner p = Partner.PARTNER;
		Employee e = Employee.EMPLOYEE;
		return new QuoteResponse(
			r.get(q.ID),
			r.get(q.QUOTE_NUMBER),
			r.get(q.QUOTE_NUMBER),
			r.get(q.QUOTE_DATE),
			r.get(q.VALID_UNTIL),
			r.get(q.PARTNER_ID),
			r.get(p.CODE),
			r.get(p.NAME),
			r.get(q.EMPLOYEE_ID),
			r.get(e.CODE),
			r.get(e.NAME),
			r.get(q.STATUS_CODE),
			r.get(q.REMARKS),
			lines != null ? lines : List.of()
		);
	}
}
