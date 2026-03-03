package com.github.gwiman.mini_mes_backend.quote.domain;

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

@Entity
@Table(name = "quote")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String quoteNumber;

	@Column(nullable = false)
	private LocalDate quoteDate;

	private LocalDate validUntil;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_id", nullable = false)
	private Partner partner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@Column(length = 20)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<QuoteLine> lines = new ArrayList<>();

	public Quote(String quoteNumber, LocalDate quoteDate, LocalDate validUntil,
		Partner partner, Employee employee, String statusCode, String remarks) {
		this.quoteNumber = quoteNumber;
		this.quoteDate = quoteDate;
		this.validUntil = validUntil;
		this.partner = partner;
		this.employee = employee;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	public void update(LocalDate quoteDate, LocalDate validUntil,
		Partner partner, Employee employee, String statusCode, String remarks) {
		this.quoteDate = quoteDate;
		this.validUntil = validUntil;
		this.partner = partner;
		this.employee = employee;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	public void addLine(QuoteLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}
}
