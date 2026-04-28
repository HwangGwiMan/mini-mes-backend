package com.github.gwiman.mini_mes_backend.materialissue.domain;

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

/**
 * 자재 출고 헤더 엔티티.
 * <p>
 * 작업지시(CONFIRMED) 1건당 자재 출고 1건이 생성된다(1:1 제약은 서비스에서 보장).
 * 확정(CONFIRMED) 시 각 라인의 재고를 실제 차감(PRODUCTION_OUT: qty_on_hand--, qty_reserved--)한다.
 * 상태 흐름: DRAFT → CONFIRMED / CANCELLED
 * </p>
 */
@Entity
@Table(name = "material_issue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaterialIssue extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 자재 출고 번호 — 채번 규칙: MI_YYYYMM_NNN */
	@Column(unique = true, nullable = false, length = 50)
	private String materialIssueNumber;

	/** 연결 작업지시 ID — unique 제약으로 1:1 보장 */
	@Column(nullable = false, unique = true)
	private Long workOrderId;

	@Column(name = "status_code", length = 20, nullable = false)
	private MaterialIssueStatus status;

	@Column(nullable = false)
	private LocalDate issueDate;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "materialIssue", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<MaterialIssueLine> lines = new ArrayList<>();

	private MaterialIssue(String materialIssueNumber, Long workOrderId,
			LocalDate issueDate, String remarks) {
		this.materialIssueNumber = materialIssueNumber;
		this.workOrderId = workOrderId;
		this.status = MaterialIssueStatus.DRAFT;
		this.issueDate = issueDate;
		this.remarks = remarks != null ? remarks : "";
	}

	/** 자재 출고 생성 — 항상 DRAFT로 시작 */
	public static MaterialIssue create(String materialIssueNumber, Long workOrderId,
			LocalDate issueDate, String remarks) {
		return new MaterialIssue(materialIssueNumber, workOrderId, issueDate, remarks);
	}

	/** 수정 — DRAFT 상태만 허용 */
	public void update(LocalDate issueDate, String remarks) {
		if (!canEdit()) {
			throw new BusinessRuleViolationException("초안 상태에서만 자재 출고를 수정할 수 있습니다.");
		}
		this.issueDate = issueDate;
		this.remarks = remarks != null ? remarks : "";
	}

	/** DRAFT → CONFIRMED (재고 차감은 서비스 레이어에서 처리) */
	public void confirm() {
		if (this.status != MaterialIssueStatus.DRAFT) {
			throw new BusinessRuleViolationException("초안 상태에서만 자재 출고를 확정할 수 있습니다.");
		}
		this.status = MaterialIssueStatus.CONFIRMED;
	}

	/** DRAFT / CONFIRMED → CANCELLED */
	public void cancel() {
		if (this.status == MaterialIssueStatus.CANCELLED) {
			throw new BusinessRuleViolationException("이미 취소된 자재 출고입니다.");
		}
		this.status = MaterialIssueStatus.CANCELLED;
	}

	public void addLine(MaterialIssueLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}

	public boolean canEdit() {
		return this.status == MaterialIssueStatus.DRAFT;
	}

	public boolean canDelete() {
		return this.status == MaterialIssueStatus.DRAFT;
	}
}
