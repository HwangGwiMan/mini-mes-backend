package com.github.gwiman.mini_mes_backend.inventory.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.goodsreceipt.application.StockReceivedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 재고 원장 이벤트 핸들러 — 타 도메인에서 발행한 재고 변경 이벤트를 수신하여 재고를 반영한다.
 * <p>
 * {@code @ApplicationModuleListener}는 Spring Modulith의 비동기 트랜잭션 이벤트 리스너로,
 * 발행 트랜잭션이 커밋된 후 별도 트랜잭션에서 실행된다.
 * 처리 실패 시 Spring Modulith가 재시도를 관리한다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventHandler {

    private final InventoryService inventoryService;

    /**
     * 자재 입고 확정 이벤트 수신 — qty_on_hand 증가.
     * {@link StockReceivedEvent}는 goodsreceipt 모듈에서 발행되며,
     * inventory 모듈은 goodsreceipt::application NamedInterface를 통해 접근한다.
     */
    @ApplicationModuleListener
    public void onStockReceived(StockReceivedEvent event) {
        log.info("재고 입고 이벤트 수신: goodsReceiptId={}, warehouseId={}, lines={}",
                event.goodsReceiptId(), event.warehouseId(), event.lines().size());
        inventoryService.receiveStock(event);
    }
}
