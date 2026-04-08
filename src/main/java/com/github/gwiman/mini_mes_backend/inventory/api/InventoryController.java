package com.github.gwiman.mini_mes_backend.inventory.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.github.gwiman.mini_mes_backend.inventory.api.dto.AdjustRequest;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryLotResponse;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryResponse;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.InventoryTxResponse;
import com.github.gwiman.mini_mes_backend.inventory.api.dto.TransferRequest;
import com.github.gwiman.mini_mes_backend.inventory.application.InventoryService;
import com.github.gwiman.mini_mes_backend.inventory.domain.InventoryTxType;
import com.github.gwiman.mini_mes_backend.inventory.internal.InventoryQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 재고 원장 API.
 * 읽기 전용 조회는 {@link InventoryQueryRepository}를 직접 사용하고,
 * 재고 변경(이동·조정)은 {@link InventoryService}를 통해 처리한다.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryQueryRepository inventoryQueryRepository;

    // -------------------------------------------------------------------------
    // 조회
    // -------------------------------------------------------------------------

    /**
     * 현재고 목록 조회 — 품목×창고 집계 단위.
     *
     * @param warehouseId 창고 ID (선택)
     * @param itemId      품목 ID (선택)
     */
    @GetMapping
    public List<InventoryResponse> getInventory(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long itemId) {
        return inventoryQueryRepository.searchInventory(warehouseId, itemId);
    }

    /**
     * LOT별 현재고 조회.
     *
     * @param warehouseId 창고 ID (선택)
     * @param itemId      품목 ID (선택)
     */
    @GetMapping("/lots")
    public List<InventoryLotResponse> getInventoryLots(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long itemId) {
        return inventoryQueryRepository.searchInventoryLots(warehouseId, itemId);
    }

    /**
     * 수불 이력 조회.
     *
     * @param warehouseId 창고 ID (선택)
     * @param itemId      품목 ID (선택)
     * @param fromDate    조회 시작일 (선택, yyyy-MM-dd)
     * @param toDate      조회 종료일 (선택, yyyy-MM-dd)
     */
    @GetMapping("/transactions")
    public List<InventoryTxResponse> getTxHistory(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return inventoryQueryRepository.searchTxHistory(warehouseId, itemId, fromDate, toDate);
    }

    // -------------------------------------------------------------------------
    // 재고 변경
    // -------------------------------------------------------------------------

    /**
     * 창고 간 재고 이동.
     * TRANSFER_OUT + TRANSFER_IN 두 수불 이력이 생성되며 transferId로 쌍이 연결된다.
     */
    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@RequestBody @Valid TransferRequest request) {
        inventoryService.transfer(
                request.fromWarehouseId(),
                request.toWarehouseId(),
                request.itemId(),
                request.lotNo(),
                request.qty(),
                null);
    }

    /**
     * 재고 조정 — 실사 결과 반영 또는 손망실 처리.
     * txType: "ADJUST_IN" (증가) 또는 "ADJUST_OUT" (감소)
     */
    @PostMapping("/adjust")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adjust(@RequestBody @Valid AdjustRequest request) {
        InventoryTxType txType = InventoryTxType.valueOf(request.txType());
        inventoryService.adjust(
                request.warehouseId(),
                request.itemId(),
                request.lotNo(),
                request.qty(),
                txType,
                null);
    }
}
