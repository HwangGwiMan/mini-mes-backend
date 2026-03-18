package com.github.gwiman.mini_mes_backend.revenue.domain;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매출 헤더 엔티티.
 * 담당자가 거래처를 선택하고 해당 거래처의 완료 수주 품목을 골라 수동으로 생성한다.
 * REVENUE_STATUS 공통코드로 상태(초안→마감/취소)를 관리한다.
 * 매출 1건은 동일 거래처의 여러 수주 라인을 포함할 수 있다.
 */
@Entity
@Table(name = "revenue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Revenue extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 자동채번: RE_YYYYMM_001 */
	@Column(unique = true, nullable = false, length = 50)
	private String revenueNumber;

	/** 매출 대상 거래처 — 품목 선택 기준 */
	@Column(name = "partner_id", nullable = false)
	private Long partnerId;

	@Column(name = "employee_id")
	private Long employeeId;

	@Column(nullable = false)
	private LocalDate revenueDate;

	/** REVENUE_STATUS 공통코드 참조 */
	@Column(length = 20, nullable = false)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "revenue", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<RevenueLine> lines = new ArrayList<>();

	public Revenue(String revenueNumber, Long partnerId, Long employeeId,
		LocalDate revenueDate, String statusCode, String remarks) {
		this.revenueNumber = revenueNumber;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.revenueDate = revenueDate;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	/** 초안 상태에서만 허용 */
	public void update(Long employeeId, LocalDate revenueDate, String remarks) {
		this.employeeId = employeeId;
		this.revenueDate = revenueDate;
		this.remarks = remarks;
	}

	/** 초안 → 마감 */
	public void close() {
		this.statusCode = "REVENUE_STATUS_02";
	}

	/** 마감 → 취소 */
	public void cancel() {
		this.statusCode = "REVENUE_STATUS_03";
	}

	public void addLine(RevenueLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}
}
