package com.github.gwiman.mini_mes_backend.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 품목 × 창고 × LOT 단위 현재고 스냅샷 엔티티 — ADR-004 LOT 관리 정책.
 * <p>
 * LOT가 지정된 경우에만 생성되며, {@link Inventory}와 함께 같은 트랜잭션에서 갱신한다.
 * 자재 입고 시 lotNo가 null이면 이 테이블은 갱신하지 않는다.
 * </p>
 */
@Entity
@Table(name = "inventory_lot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "item_id", "lot_no"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryLot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 50)
    private String lotNo;

    /** 실물 보유량 */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyOnHand;

    /** 선점(예약)량 */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyReserved;

    /** 유통기한 — 선택 */
    private LocalDate expiryDate;

    private InventoryLot(Long warehouseId, Long itemId, String lotNo, LocalDate expiryDate) {
        this.warehouseId = warehouseId;
        this.itemId = itemId;
        this.lotNo = lotNo;
        this.expiryDate = expiryDate;
        this.qtyOnHand = BigDecimal.ZERO;
        this.qtyReserved = BigDecimal.ZERO;
    }

    /** 신규 LOT 재고 레코드 생성 */
    public static InventoryLot create(Long warehouseId, Long itemId, String lotNo, LocalDate expiryDate) {
        return new InventoryLot(warehouseId, itemId, lotNo, expiryDate);
    }

    /** 입고 — qty_on_hand 증가 */
    public void receive(BigDecimal qtyDelta) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("입고 수량은 양수여야 합니다.");
        }
        this.qtyOnHand = this.qtyOnHand.add(qtyDelta);
    }

    /**
     * 선점 — qty_reserved 증가.
     * LOT 가용 재고가 충분해야 한다.
     */
    public void reserve(BigDecimal qtyDelta) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("선점 수량은 양수여야 합니다.");
        }
        BigDecimal available = this.qtyOnHand.subtract(this.qtyReserved);
        if (available.compareTo(qtyDelta) < 0) {
            throw new BusinessRuleViolationException(
                    "LOT 가용 재고가 부족합니다. LOT: " + lotNo + ", 가용: " + available + ", 요청: " + qtyDelta);
        }
        this.qtyReserved = this.qtyReserved.add(qtyDelta);
    }

    /** 선점 해제 — qty_reserved 감소 */
    public void unreserve(BigDecimal qtyDelta) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("선점 해제 수량은 양수여야 합니다.");
        }
        if (this.qtyReserved.compareTo(qtyDelta) < 0) {
            throw new BusinessRuleViolationException(
                    "선점 수량보다 해제 수량이 클 수 없습니다. LOT: " + lotNo);
        }
        this.qtyReserved = this.qtyReserved.subtract(qtyDelta);
    }

    /**
     * 출고 확정 — qty_on_hand 감소, 필요 시 qty_reserved 감소.
     *
     * @param releaseReservation true면 선점도 함께 해제(PRODUCTION_OUT), false면 순수 출고(SALES_OUT)
     */
    public void issue(BigDecimal qtyDelta, boolean releaseReservation) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("출고 수량은 양수여야 합니다.");
        }
        if (this.qtyOnHand.compareTo(qtyDelta) < 0) {
            throw new BusinessRuleViolationException(
                    "LOT 재고가 부족합니다. LOT: " + lotNo + ", 보유: " + this.qtyOnHand + ", 출고: " + qtyDelta);
        }
        this.qtyOnHand = this.qtyOnHand.subtract(qtyDelta);
        if (releaseReservation) {
            if (this.qtyReserved.compareTo(qtyDelta) < 0) {
                throw new BusinessRuleViolationException(
                        "선점 수량보다 출고 수량이 클 수 없습니다. LOT: " + lotNo);
            }
            this.qtyReserved = this.qtyReserved.subtract(qtyDelta);
        }
    }

    /** 가용 재고 = qty_on_hand - qty_reserved */
    public BigDecimal getAvailableQty() {
        return this.qtyOnHand.subtract(this.qtyReserved);
    }
}
