package com.github.gwiman.mini_mes_backend.inventory.application;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.goodsreceipt.application.StockReceivedEvent;
import com.github.gwiman.mini_mes_backend.inventory.domain.Inventory;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryLot;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryLotRepository;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryRepository;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryTx;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryTxRepository;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryTxType;

import lombok.RequiredArgsConstructor;

/**
 * 재고 원장 서비스 — 모든 재고 변경의 진입점.
 * <p>
 * 스냅샷({@link Inventory}, {@link InventoryLot})과 수불 이력({@link InventoryTx})을
 * 항상 같은 트랜잭션에서 동시에 갱신한다.
 * 스냅샷에 낙관적 락 충돌이 발생하면 {@code ObjectOptimisticLockingFailureException}이
 * 전파되며, 호출 측에서 재시도 처리를 담당한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLotRepository inventoryLotRepository;
    private final InventoryTxRepository inventoryTxRepository;

    // -------------------------------------------------------------------------
    // 구매 입고 (PURCHASE_IN)
    // -------------------------------------------------------------------------

    /**
     * 자재 입고 확정 이벤트를 처리하여 재고를 반영한다.
     * <p>
     * 각 라인에 대해:
     * <ul>
     *   <li>{@link Inventory} 스냅샷을 upsert — 없으면 신규 생성</li>
     *   <li>{@code lotNo}가 있으면 {@link InventoryLot} 스냅샷도 upsert</li>
     *   <li>{@link InventoryTx} 원장에 PURCHASE_IN 레코드 삽입</li>
     * </ul>
     * </p>
     */
    @Transactional
    public void receiveStock(StockReceivedEvent event) {
        LocalDate today = LocalDate.now();

        for (StockReceivedEvent.Line line : event.lines()) {
            Long warehouseId = event.warehouseId();
            Long itemId = line.itemId();
            BigDecimal qty = line.receivedQty();

            // 스냅샷 upsert
            Inventory inv = inventoryRepository
                    .findByWarehouseIdAndItemId(warehouseId, itemId)
                    .orElseGet(() -> inventoryRepository.save(Inventory.create(warehouseId, itemId)));
            inv.receive(qty);

            // LOT 스냅샷 upsert (LOT 지정 입고인 경우)
            if (line.lotNo() != null) {
                InventoryLot lot = inventoryLotRepository
                        .findByWarehouseIdAndItemIdAndLotNo(warehouseId, itemId, line.lotNo())
                        .orElseGet(() -> inventoryLotRepository.save(
                                InventoryLot.create(warehouseId, itemId, line.lotNo(), null)));
                lot.receive(qty);
            }

            // 수불 원장 기록
            inventoryTxRepository.save(InventoryTx.create(
                    warehouseId, itemId, line.lotNo(),
                    InventoryTxType.PURCHASE_IN, qty,
                    "PURCHASE_ORDER", event.goodsReceiptId(),
                    today));
        }
    }

    // -------------------------------------------------------------------------
    // 창고 간 이동 (TRANSFER_OUT + TRANSFER_IN)
    // -------------------------------------------------------------------------

    /**
     * 창고 간 재고 이동.
     * <p>
     * OUT·IN 두 {@link InventoryTx} 레코드를 생성하고 {@code transferId}로 쌍을 연결한다.
     * LOT 단위 이동 — {@code lotNo}는 필수이다.
     * </p>
     *
     * @param fromWarehouseId 출발 창고
     * @param toWarehouseId   도착 창고
     * @param itemId          품목
     * @param lotNo           이동 LOT (필수)
     * @param qty             이동 수량
     * @param refId           이동 근거 문서 ID (선택)
     */
    @Transactional
    public void transfer(Long fromWarehouseId, Long toWarehouseId,
            Long itemId, String lotNo, BigDecimal qty, Long refId) {

        LocalDate today = LocalDate.now();

        // 출발 창고 스냅샷 감소
        Inventory fromInv = inventoryRepository
                .findByWarehouseIdAndItemId(fromWarehouseId, itemId)
                .orElseThrow(() -> new com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException(
                        "출발 창고 재고를 찾을 수 없습니다. warehouseId=" + fromWarehouseId + ", itemId=" + itemId));
        fromInv.issue(qty, false);

        if (lotNo != null) {
            InventoryLot fromLot = inventoryLotRepository
                    .findByWarehouseIdAndItemIdAndLotNo(fromWarehouseId, itemId, lotNo)
                    .orElseThrow(() -> new com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException(
                            "출발 창고 LOT 재고를 찾을 수 없습니다. LOT=" + lotNo));
            fromLot.issue(qty, false);
        }

        // 도착 창고 스냅샷 증가
        Inventory toInv = inventoryRepository
                .findByWarehouseIdAndItemId(toWarehouseId, itemId)
                .orElseGet(() -> inventoryRepository.save(Inventory.create(toWarehouseId, itemId)));
        toInv.receive(qty);

        if (lotNo != null) {
            InventoryLot toLot = inventoryLotRepository
                    .findByWarehouseIdAndItemIdAndLotNo(toWarehouseId, itemId, lotNo)
                    .orElseGet(() -> inventoryLotRepository.save(
                            InventoryLot.create(toWarehouseId, itemId, lotNo, null)));
            toLot.receive(qty);
        }

        // 수불 원장 — OUT/IN 쌍 기록 후 transferId 연결
        InventoryTx outTx = inventoryTxRepository.save(InventoryTx.create(
                fromWarehouseId, itemId, lotNo,
                InventoryTxType.TRANSFER_OUT, qty,
                "TRANSFER", refId, today));

        InventoryTx inTx = inventoryTxRepository.save(InventoryTx.create(
                toWarehouseId, itemId, lotNo,
                InventoryTxType.TRANSFER_IN, qty,
                "TRANSFER", refId, today));

        // 같은 transferId로 쌍 연결 (OUT 레코드의 id를 사용)
        outTx.linkTransfer(outTx.getId());
        inTx.linkTransfer(outTx.getId());
    }

    // -------------------------------------------------------------------------
    // 재고 조정 (ADJUST_IN / ADJUST_OUT)
    // -------------------------------------------------------------------------

    /**
     * 재고 조정 — 실사 결과 또는 손망실 반영.
     *
     * @param warehouseId 창고
     * @param itemId      품목
     * @param lotNo       LOT (null 허용)
     * @param qtyDelta    조정 수량 (양수)
     * @param txType      {@link InventoryTxType#ADJUST_IN} 또는 {@link InventoryTxType#ADJUST_OUT}
     * @param refId       근거 문서 ID (선택)
     */
    @Transactional
    public void adjust(Long warehouseId, Long itemId, String lotNo,
            BigDecimal qtyDelta, InventoryTxType txType, Long refId) {

        com.github.gwiman.mini_mes_backend.common.util.Guard.require(
                txType == InventoryTxType.ADJUST_IN || txType == InventoryTxType.ADJUST_OUT,
                "조정 유형은 ADJUST_IN 또는 ADJUST_OUT만 허용됩니다.");

        LocalDate today = LocalDate.now();

        Inventory inv = inventoryRepository
                .findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseGet(() -> inventoryRepository.save(Inventory.create(warehouseId, itemId)));

        if (txType == InventoryTxType.ADJUST_IN) {
            inv.receive(qtyDelta);
        } else {
            inv.issue(qtyDelta, false);
        }

        if (lotNo != null) {
            InventoryLot lot = inventoryLotRepository
                    .findByWarehouseIdAndItemIdAndLotNo(warehouseId, itemId, lotNo)
                    .orElseGet(() -> inventoryLotRepository.save(
                            InventoryLot.create(warehouseId, itemId, lotNo, null)));
            if (txType == InventoryTxType.ADJUST_IN) {
                lot.receive(qtyDelta);
            } else {
                lot.issue(qtyDelta, false);
            }
        }

        inventoryTxRepository.save(InventoryTx.create(
                warehouseId, itemId, lotNo,
                txType, qtyDelta,
                "ADJUST", refId, today));
    }

    // -------------------------------------------------------------------------
    // 스터브 — Phase C에서 시그니처만 정의, Phase D (작업지시/자재출고)에서 구현
    // -------------------------------------------------------------------------

    /**
     * 작업지시 확정 시 투입 자재 선점 — {@code MATERIAL_RESERVE}.
     * <p>
     * Inventory 스냅샷의 {@code qty_reserved}를 증가시킨다.
     * 가용 재고(qty_on_hand - qty_reserved)가 부족하면 예외가 발생한다.
     * </p>
     */
    @Transactional
    public void reserveMaterial(Long warehouseId, Long itemId,
            BigDecimal qty, Long workOrderId) {
        Inventory inv = inventoryRepository
                .findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseGet(() -> inventoryRepository.save(Inventory.create(warehouseId, itemId)));
        inv.reserve(qty);

        inventoryTxRepository.save(InventoryTx.create(
                warehouseId, itemId, null,
                InventoryTxType.MATERIAL_RESERVE, qty,
                "WORK_ORDER", workOrderId,
                LocalDate.now()));
    }

    /**
     * 작업지시 취소 시 선점 해제 — {@code MATERIAL_UNRESERVE}.
     * <p>
     * Inventory 스냅샷의 {@code qty_reserved}를 감소시킨다.
     * </p>
     */
    @Transactional
    public void unreserveMaterial(Long warehouseId, Long itemId,
            BigDecimal qty, Long workOrderId) {
        Inventory inv = inventoryRepository
                .findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseThrow(() -> new com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException(
                        "재고를 찾을 수 없습니다. warehouseId=" + warehouseId + ", itemId=" + itemId));
        inv.unreserve(qty);

        inventoryTxRepository.save(InventoryTx.create(
                warehouseId, itemId, null,
                InventoryTxType.MATERIAL_UNRESERVE, qty,
                "WORK_ORDER", workOrderId,
                LocalDate.now()));
    }

    /**
     * 자재 출고 확정 — {@code PRODUCTION_OUT}.
     * Phase D 구현 시 완성.
     */
    @Transactional
    public void issueMaterial(Long warehouseId, Long itemId, String lotNo,
            BigDecimal qty, Long materialIssueId) {
        throw new UnsupportedOperationException("Phase D에서 구현 예정");
    }

    /**
     * 생산 완료 입고 — {@code PRODUCTION_IN}.
     * Phase D 구현 시 완성.
     */
    @Transactional
    public void receiveProduction(Long warehouseId, Long itemId, String lotNo,
            BigDecimal qty, Long workOrderId) {
        throw new UnsupportedOperationException("Phase D에서 구현 예정");
    }

    /**
     * 판매 출고 확정 — {@code SALES_OUT}.
     * Phase D 구현 시 완성.
     */
    @Transactional
    public void issueSales(Long warehouseId, Long itemId, String lotNo,
            BigDecimal qty, Long salesOrderItemId) {
        throw new UnsupportedOperationException("Phase D에서 구현 예정");
    }

}
