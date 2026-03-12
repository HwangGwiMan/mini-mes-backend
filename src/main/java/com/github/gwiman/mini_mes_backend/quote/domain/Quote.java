package com.github.gwiman.mini_mes_backend.quote.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quote")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String quoteNumber;

	@Column(nullable = false)
	private LocalDate quoteDate;

	private LocalDate validUntil;

	@Column(name = "partner_id", nullable = false)
	private Long partnerId;

	@Column(name = "employee_id")
	private Long employeeId;

	@Column(name = "approver_id", nullable = false)
	private Long approverId;

	@Column(length = 20)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<QuoteLine> lines = new ArrayList<>();

	public Quote(String quoteNumber, LocalDate quoteDate, LocalDate validUntil,
		Long partnerId, Long employeeId, Long approverId, String statusCode, String remarks) {
		this.quoteNumber = quoteNumber;
		this.quoteDate = quoteDate;
		this.validUntil = validUntil;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.approverId = approverId;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	public void update(LocalDate quoteDate, LocalDate validUntil,
		Long partnerId, Long employeeId, Long approverId, String remarks) {
		if ("QUOTE_STATUS_02".equals(this.statusCode)) {
			throw new BusinessRuleViolationException("제출된 견적은 수정할 수 없습니다.");
		}
		this.quoteDate = quoteDate;
		this.validUntil = validUntil;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.approverId = approverId;
		this.remarks = remarks;
	}

	public void addLine(QuoteLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}

	public void updateStatus(String statusCode) {
		this.statusCode = statusCode;
	}
}
