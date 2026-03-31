package com.github.gwiman.mini_mes_backend.purchaseorder.domain;

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
 * 구매 발주 헤더 엔티티.
 * <p>
 * 거래처에 자재 구매를 발주하는 문서. 구매 요청(PurchaseRequest) 전환 또는 직접 생성이 가능하다.
 * 상태 흐름: DRAFT → ORDERED → RECEIVED / CANCELLED
 * </p>
 */
@Entity
@Table(name = "purchase_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String orderNumber;

	@Column(nullable = false)
	private LocalDate orderDate;

	@Column(nullable = false)
	private Long partnerId;

	private LocalDate expectedArrivalDate;

	@Column(name = "status_code", length = 20, nullable = false)
	private PurchaseOrderStatus status;

	/** 구매 요청 전환 시 원본 PR ID — 직접 생성 시 null */
	@Column(name = "pr_id")
	private Long prId;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<PurchaseOrderLine> lines = new ArrayList<>();

	private PurchaseOrder(String orderNumber, LocalDate orderDate, Long partnerId,
			LocalDate expectedArrivalDate, PurchaseOrderStatus status, Long prId, String remarks) {
		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.partnerId = partnerId;
		this.expectedArrivalDate = expectedArrivalDate;
		this.status = status;
		this.prId = prId;
		this.remarks = remarks != null ? remarks : "";
	}

	/** 직접 생성 — prId null */
	public static PurchaseOrder create(String orderNumber, LocalDate orderDate,
			Long partnerId, LocalDate expectedArrivalDate, String remarks) {
		return new PurchaseOrder(orderNumber, orderDate, partnerId,
				expectedArrivalDate, PurchaseOrderStatus.DRAFT, null, remarks);
	}

	/** 구매 요청 전환 생성 — prId 기록 */
	public static PurchaseOrder fromPurchaseRequest(String orderNumber, LocalDate orderDate,
			Long partnerId, LocalDate expectedArrivalDate, Long prId, String remarks) {
		return new PurchaseOrder(orderNumber, orderDate, partnerId,
				expectedArrivalDate, PurchaseOrderStatus.DRAFT, prId, remarks);
	}

	/** 수정 — DRAFT 또는 CANCELLED 상태만 허용 */
	public void update(LocalDate orderDate, Long partnerId,
			LocalDate expectedArrivalDate, String remarks) {
		if (!canEdit()) {
			throw new BusinessRuleViolationException("초안 또는 취소 상태의 발주만 수정할 수 있습니다.");
		}
		this.orderDate = orderDate;
		this.partnerId = partnerId;
		this.expectedArrivalDate = expectedArrivalDate;
		this.remarks = remarks != null ? remarks : "";
	}

	/** DRAFT → ORDERED */
	public void confirm() {
		if (this.status != PurchaseOrderStatus.DRAFT) {
			throw new BusinessRuleViolationException("초안 상태의 발주만 확정할 수 있습니다.");
		}
		this.status = PurchaseOrderStatus.ORDERED;
	}

	/** DRAFT 또는 ORDERED → CANCELLED */
	public void cancel() {
		if (!canCancel()) {
			throw new BusinessRuleViolationException("초안 또는 발주됨 상태에서만 취소할 수 있습니다.");
		}
		this.status = PurchaseOrderStatus.CANCELLED;
	}

	/**
	 * ORDERED → RECEIVED.
	 * 자재입고(GoodsReceipt) 확정 시 GoodsReceiptService에서 호출한다.
	 */
	public void markReceived() {
		if (this.status != PurchaseOrderStatus.ORDERED) {
			throw new BusinessRuleViolationException("발주됨 상태의 발주만 입고완료 처리할 수 있습니다.");
		}
		this.status = PurchaseOrderStatus.RECEIVED;
	}

	public void addLine(PurchaseOrderLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}

	/** DRAFT 또는 CANCELLED 상태만 수정 가능 */
	public boolean canEdit() {
		return this.status == PurchaseOrderStatus.DRAFT || this.status == PurchaseOrderStatus.CANCELLED;
	}

	/** DRAFT 상태만 삭제 가능 */
	public boolean canDelete() {
		return this.status == PurchaseOrderStatus.DRAFT;
	}

	/** DRAFT 또는 ORDERED 상태만 취소 가능 */
	public boolean canCancel() {
		return this.status == PurchaseOrderStatus.DRAFT || this.status == PurchaseOrderStatus.ORDERED;
	}
}
