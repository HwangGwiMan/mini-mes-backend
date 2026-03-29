package com.github.gwiman.mini_mes_backend.purchaserequest.domain;

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
 * 구매 요청 헤더 엔티티.
 * <p>
 * 내부 사원이 자재 구매를 요청하는 문서. 승인 후 구매 발주(PurchaseOrder)로 전환된다.
 * 승인자 지정 없이 누구나 승인 가능하며, 별도 승인 이력 테이블 없이 BaseEntity 감사 필드로 추적한다.
 * </p>
 */
@Entity
@Table(name = "purchase_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String requestNumber;

	@Column(nullable = false)
	private LocalDate requestDate;

	/** 요청자 사원 ID — 필수는 아니나 담당자 추적을 위해 권장 */
	@Column(name = "requester_id")
	private Long requesterId;

	/** PR_STATUS 공통코드 참조 */
	@Column(length = 20, nullable = false)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<PurchaseRequestLine> lines = new ArrayList<>();

	private PurchaseRequest(String requestNumber, LocalDate requestDate,
			Long requesterId, String statusCode, String remarks) {
		this.requestNumber = requestNumber;
		this.requestDate = requestDate;
		this.requesterId = requesterId;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	/** 신규 구매 요청 생성 — 초기 상태는 항상 초안(PR_STATUS_01) */
	public static PurchaseRequest create(String requestNumber, LocalDate requestDate,
			Long requesterId, String remarks) {
		return new PurchaseRequest(requestNumber, requestDate,
				requesterId, "PR_STATUS_01", remarks != null ? remarks : "");
	}

	/**
	 * 구매 요청 헤더 수정.
	 * 초안(01) 또는 반려됨(04) 상태에서만 수정 가능 — 검토중 이후 수정 방지
	 */
	public void update(LocalDate requestDate, Long requesterId, String remarks) {
		if (!canEdit()) {
			throw new BusinessRuleViolationException("초안 또는 반려 상태의 구매 요청만 수정할 수 있습니다.");
		}
		this.requestDate = requestDate;
		this.requesterId = requesterId;
		this.remarks = remarks != null ? remarks : "";
	}

	public void addLine(PurchaseRequestLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}

	/** 초안(01) 또는 반려됨(04) → 검토중(02) */
	public void submit() {
		if (!canSubmit()) {
			throw new BusinessRuleViolationException("초안 또는 반려 상태의 구매 요청만 제출할 수 있습니다.");
		}
		this.statusCode = "PR_STATUS_02";
	}

	/** 검토중(02) → 승인됨(03) */
	public void approve() {
		if (!canApprove()) {
			throw new BusinessRuleViolationException("검토중 상태의 구매 요청만 승인할 수 있습니다.");
		}
		this.statusCode = "PR_STATUS_03";
	}

	/** 검토중(02) → 반려됨(04) */
	public void reject() {
		if (!canApprove()) {
			throw new BusinessRuleViolationException("검토중 상태의 구매 요청만 반려할 수 있습니다.");
		}
		this.statusCode = "PR_STATUS_04";
	}

	/**
	 * 승인됨(03) → 발주됨(05).
	 * 구매 발주 전환 시 PurchaseOrderService에서 내부적으로 호출한다.
	 */
	public void markOrdered() {
		if (!"PR_STATUS_03".equals(this.statusCode)) {
			throw new BusinessRuleViolationException("승인된 구매 요청만 발주 전환할 수 있습니다.");
		}
		this.statusCode = "PR_STATUS_05";
	}

	/** 초안(01) 또는 반려됨(04) 상태만 수정/삭제 가능 */
	public boolean canEdit() {
		return "PR_STATUS_01".equals(statusCode) || "PR_STATUS_04".equals(statusCode);
	}

	/** 초안(01) 또는 반려됨(04) 상태만 제출 가능 */
	public boolean canSubmit() {
		return "PR_STATUS_01".equals(statusCode) || "PR_STATUS_04".equals(statusCode);
	}

	/** 검토중(02) 상태만 승인/반려 가능 */
	public boolean canApprove() {
		return "PR_STATUS_02".equals(statusCode);
	}
}
