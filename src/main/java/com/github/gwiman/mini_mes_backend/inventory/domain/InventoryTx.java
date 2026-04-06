package com.github.gwiman.mini_mes_backend.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 수불 이력 원장 엔티티 — ADR-004 혼합 방식의 불변 원장 테이블.
 * <p>
 * 한 번 기록된 레코드는 절대 수정·삭제하지 않는다.
 * {@link BaseEntity}를 상속하지 않으며, 생성 시각·생성자만 감사 필드로 갖는다.
 * {@code @Version}(낙관적 락) 없음 — 원장은 INSERT 전용이므로 충돌 없음.
 * 창고 이동은 OUT·IN 두 레코드를 생성하고 {@code transferId}로 쌍을 연결한다.
 * </p>
 */
@Entity
@Table(name = "inventory_tx")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryTx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long itemId;

    /** LOT 번호 — LOT 미지정 입고 시 null */
    @Column(length = 50)
    private String lotNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryTxType txType;

    /** 수불 수량 — 항상 양수, 방향은 txType으로 결정 */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyDelta;

    /** 출처 유형 (예: PURCHASE_ORDER, WORK_ORDER, TRANSFER, ADJUST) */
    @Column(length = 30)
    private String refType;

    /** 출처 엔티티 ID */
    private Long refId;

    /** 창고 이동 시 OUT-IN 쌍 연결 ID */
    private Long transferId;

    /** 수불 발생 일자 */
    @Column(nullable = false)
    private LocalDate txDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false, length = 50)
    private String createdBy;

    private InventoryTx(Long warehouseId, Long itemId, String lotNo,
            InventoryTxType txType, BigDecimal qtyDelta,
            String refType, Long refId, LocalDate txDate) {
        this.warehouseId = warehouseId;
        this.itemId = itemId;
        this.lotNo = lotNo;
        this.txType = txType;
        this.qtyDelta = qtyDelta;
        this.refType = refType;
        this.refId = refId;
        this.txDate = txDate;
    }

    /** 수불 이력 레코드 생성 */
    public static InventoryTx create(Long warehouseId, Long itemId, String lotNo,
            InventoryTxType txType, BigDecimal qtyDelta,
            String refType, Long refId, LocalDate txDate) {
        return new InventoryTx(warehouseId, itemId, lotNo, txType, qtyDelta, refType, refId, txDate);
    }

    /**
     * 창고 이동 OUT-IN 쌍 연결.
     * OUT 레코드와 IN 레코드를 저장한 후 호출하여 같은 transferId를 부여한다.
     */
    public void linkTransfer(Long transferId) {
        this.transferId = transferId;
    }
}
