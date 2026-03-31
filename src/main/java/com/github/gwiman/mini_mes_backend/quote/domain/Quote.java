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

	@Column(name = "status_code", length = 20)
	private QuoteStatus status;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<QuoteLine> lines = new ArrayList<>();

	private Quote(String quoteNumber, LocalDate quoteDate, LocalDate validUntil,
		Long partnerId, Long employeeId, Long approverId, QuoteStatus status, String remarks) {
		this.quoteNumber = quoteNumber;
		this.quoteDate = quoteDate;
		this.validUntil = validUntil;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.approverId = approverId;
		this.status = status;
		this.remarks = remarks;
	}

	/** 신규 견적 생성 — 초기 상태는 항상 작성중(DRAFT) */
	public static Quote create(String quoteNumber, LocalDate quoteDate, LocalDate validUntil,
		Long partnerId, Long employeeId, Long approverId, String remarks) {
		return new Quote(quoteNumber, quoteDate, validUntil,
			partnerId, employeeId, approverId, QuoteStatus.DRAFT, remarks != null ? remarks : "");
	}

	public void update(LocalDate quoteDate, LocalDate validUntil,
		Long partnerId, Long employeeId, Long approverId, String remarks) {
		if (this.status == QuoteStatus.SUBMITTED) {
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

	public void updateStatus(QuoteStatus status) {
		this.status = status;
	}

	/** 작성중(DRAFT) 또는 반려(REJECTED) 상태만 제출 가능 */
	public boolean canSubmit() {
		return this.status == QuoteStatus.DRAFT || this.status == QuoteStatus.REJECTED;
	}

	/** 제출(SUBMITTED) 상태만 승인/반려 가능 */
	public boolean canApprove() {
		return this.status == QuoteStatus.SUBMITTED;
	}
}
