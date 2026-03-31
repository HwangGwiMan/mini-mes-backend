// 생산실적 모듈 — 작업지시 대비 실제 생산량·불량수량 입력, 완제품/반제품 재고 반영
// 생산 완료 시 inventory 모듈에 완제품 qty_on_hand++ 이벤트 발행
// 생산 시작 조건: 하위 작업지시 전체 COMPLETED + 자재 출고 전체 CONFIRMED
@ApplicationModule(allowedDependencies = {"workorder::application", "materialissue::application", "inventory::application", "warehouse::application", "item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.production;

import org.springframework.modulith.ApplicationModule;
