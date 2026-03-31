// 재고 원장 모듈 — 스냅샷(inventory/inventory_lot) + 수불 이력(inventory_tx) 혼합 방식 (ADR-004)
// goodsreceipt(구매 입고), materialissue(자재 출고), production(생산 완료), shipment(출고 확정) 이벤트를 수신하여 재고 반영
@ApplicationModule(allowedDependencies = {"warehouse::application", "item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.inventory;

import org.springframework.modulith.ApplicationModule;
