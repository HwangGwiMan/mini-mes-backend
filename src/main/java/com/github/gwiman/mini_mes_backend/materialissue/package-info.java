// 자재 출고 모듈 — 작업지시별 투입 자재 LOT 지정 및 출고 확정
// 출고 확정 시 inventory 모듈에 qty_on_hand--, qty_reserved-- 이벤트 발행
@ApplicationModule(allowedDependencies = {"workorder::application", "inventory::application", "warehouse::application", "item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.materialissue;

import org.springframework.modulith.ApplicationModule;
