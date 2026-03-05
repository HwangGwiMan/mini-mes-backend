package com.github.gwiman.mini_mes_backend.salesorder.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.github.gwiman.mini_mes_backend.employee.domain.Employee;
import com.github.gwiman.mini_mes_backend.partner.domain.Partner;
import com.github.gwiman.mini_mes_backend.quote.domain.Quote;

@Entity
@Table(name = "sales_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String orderNumber;

	@Column(nullable = false)
	private LocalDate orderDate;

	private LocalDate deliveryDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_id", nullable = false)
	private Partner partner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quote_id")
	private Quote quote;

	@Column(length = 20)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<SalesOrderLine> lines = new ArrayList<>();

	public SalesOrder(String orderNumber, LocalDate orderDate, LocalDate deliveryDate,
		Partner partner, Employee employee, Quote quote, String statusCode, String remarks) {
		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.deliveryDate = deliveryDate;
		this.partner = partner;
		this.employee = employee;
		this.quote = quote;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	public void update(LocalDate orderDate, LocalDate deliveryDate,
		Partner partner, Employee employee, String statusCode, String remarks) {
		this.orderDate = orderDate;
		this.deliveryDate = deliveryDate;
		this.partner = partner;
		this.employee = employee;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	public void addLine(SalesOrderLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}
}
