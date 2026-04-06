package com.github.gwiman.mini_mes_backend.inventory.domain;

import java.math.BigDecimal;

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
 * 품목 × 창고 단위 현재고 스냅샷 엔티티 — ADR-004 혼합 방식의 스냅샷 테이블.
 * <p>
 * 모든 재고 변경(입고·출고·선점·해제)은 이 테이블과 {@link InventoryTx} 원장을
 * 같은 트랜잭션에서 동시에 갱신해야 한다.
 * 낙관적 락({@code @Version})으로 동시 수정 충돌을 감지한다.
 * </p>
 */
@Entity
@Table(name = "inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "item_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long itemId;

    /** 실물 보유량 */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyOnHand;

    /** 선점(예약)량 — 작업지시 등록 시 투입 자재에 설정 */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyReserved;

    private Inventory(Long warehouseId, Long itemId) {
        this.warehouseId = warehouseId;
        this.itemId = itemId;
        this.qtyOnHand = BigDecimal.ZERO;
        this.qtyReserved = BigDecimal.ZERO;
    }

    /** 신규 재고 레코드 생성 — 수량은 0으로 초기화 */
    public static Inventory create(Long warehouseId, Long itemId) {
        return new Inventory(warehouseId, itemId);
    }

    /**
     * 입고 — qty_on_hand 증가.
     * qty_delta는 반드시 양수여야 한다.
     */
    public void receive(BigDecimal qtyDelta) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("입고 수량은 양수여야 합니다.");
        }
        this.qtyOnHand = this.qtyOnHand.add(qtyDelta);
    }

    /**
     * 선점 — qty_reserved 증가.
     * 가용 재고(qty_on_hand - qty_reserved)가 충분해야 한다.
     */
    public void reserve(BigDecimal qtyDelta) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("선점 수량은 양수여야 합니다.");
        }
        BigDecimal available = this.qtyOnHand.subtract(this.qtyReserved);
        if (available.compareTo(qtyDelta) < 0) {
            throw new BusinessRuleViolationException(
                    "가용 재고가 부족합니다. 가용: " + available + ", 요청: " + qtyDelta);
        }
        this.qtyReserved = this.qtyReserved.add(qtyDelta);
    }

    /**
     * 선점 해제 — qty_reserved 감소.
     * 작업지시 취소 시 호출.
     */
    public void unreserve(BigDecimal qtyDelta) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("선점 해제 수량은 양수여야 합니다.");
        }
        if (this.qtyReserved.compareTo(qtyDelta) < 0) {
            throw new BusinessRuleViolationException(
                    "선점 수량보다 해제 수량이 클 수 없습니다. 선점: " + this.qtyReserved + ", 요청: " + qtyDelta);
        }
        this.qtyReserved = this.qtyReserved.subtract(qtyDelta);
    }

    /**
     * 출고 확정 — qty_on_hand 감소, qty_reserved 감소.
     * 자재 출고 확정(PRODUCTION_OUT) 또는 판매 출고(SALES_OUT) 시 호출.
     *
     * @param releaseReservation true면 선점도 함께 해제(PRODUCTION_OUT), false면 순수 출고(SALES_OUT)
     */
    public void issue(BigDecimal qtyDelta, boolean releaseReservation) {
        if (qtyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("출고 수량은 양수여야 합니다.");
        }
        if (this.qtyOnHand.compareTo(qtyDelta) < 0) {
            throw new BusinessRuleViolationException(
                    "재고가 부족합니다. 보유: " + this.qtyOnHand + ", 출고 요청: " + qtyDelta);
        }
        this.qtyOnHand = this.qtyOnHand.subtract(qtyDelta);
        if (releaseReservation) {
            if (this.qtyReserved.compareTo(qtyDelta) < 0) {
                throw new BusinessRuleViolationException(
                        "선점 수량보다 출고 수량이 클 수 없습니다. 선점: " + this.qtyReserved + ", 출고: " + qtyDelta);
            }
            this.qtyReserved = this.qtyReserved.subtract(qtyDelta);
        }
    }

    /** 가용 재고 = qty_on_hand - qty_reserved */
    public BigDecimal getAvailableQty() {
        return this.qtyOnHand.subtract(this.qtyReserved);
    }
}
