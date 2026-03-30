package com.github.gwiman.mini_mes_backend.goodsreceipt.domain;

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
 * 자재 입고 헤더 엔티티.
 * <p>
 * 구매 발주(PurchaseOrder) 대비 자재가 실제 입고되는 문서.
 * 발주 없이 직접 입고도 허용된다(poId nullable).
 * 상태 흐름: 초안(GR_STATUS_01) → 입고완료(GR_STATUS_02) / 취소(GR_STATUS_03)
 * 입고 확정 시 연결된 PO가 있으면 해당 PO를 입고완료(PO_STATUS_03)로 전이한다.
 * </p>
 */
@Entity
@Table(name = "goods_receipt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoodsReceipt extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String receiptNumber;

	@Column(nullable = false)
	private LocalDate receiptDate;

	/** 연결된 구매 발주 ID — 직접 입고 시 null */
	@Column(name = "po_id")
	private Long poId;

	@Column(nullable = false)
	private Long partnerId;

	/** GR_STATUS 공통코드 */
	@Column(length = 20, nullable = false)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<GoodsReceiptLine> lines = new ArrayList<>();

	private GoodsReceipt(String receiptNumber, LocalDate receiptDate,
			Long poId, Long partnerId, String remarks) {
		this.receiptNumber = receiptNumber;
		this.receiptDate = receiptDate;
		this.poId = poId;
		this.partnerId = partnerId;
		this.statusCode = "GR_STATUS_01";
		this.remarks = remarks != null ? remarks : "";
	}

	/** 자재 입고 생성 — 항상 초안(GR_STATUS_01)으로 시작 */
	public static GoodsReceipt create(String receiptNumber, LocalDate receiptDate,
			Long poId, Long partnerId, String remarks) {
		return new GoodsReceipt(receiptNumber, receiptDate, poId, partnerId, remarks);
	}

	/** 수정 — 초안(GR_STATUS_01) 상태만 허용 */
	public void update(LocalDate receiptDate, Long poId, Long partnerId, String remarks) {
		if (!canEdit()) {
			throw new BusinessRuleViolationException("초안 상태에서만 입고를 수정할 수 있습니다.");
		}
		this.receiptDate = receiptDate;
		this.poId = poId;
		this.partnerId = partnerId;
		this.remarks = remarks != null ? remarks : "";
	}

	/** 초안(GR_STATUS_01) → 입고완료(GR_STATUS_02) */
	public void confirm() {
		if (!"GR_STATUS_01".equals(this.statusCode)) {
			throw new BusinessRuleViolationException("초안 상태에서만 입고 확정할 수 있습니다.");
		}
		this.statusCode = "GR_STATUS_02";
	}

	/** 초안(GR_STATUS_01) → 취소(GR_STATUS_03) */
	public void cancel() {
		if (!"GR_STATUS_01".equals(this.statusCode)) {
			throw new BusinessRuleViolationException("초안 상태에서만 입고를 취소할 수 있습니다.");
		}
		this.statusCode = "GR_STATUS_03";
	}

	public void addLine(GoodsReceiptLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}

	/** 초안(GR_STATUS_01) 상태만 수정/삭제 가능 */
	public boolean canEdit() {
		return "GR_STATUS_01".equals(statusCode);
	}

	public boolean canDelete() {
		return "GR_STATUS_01".equals(statusCode);
	}
}
