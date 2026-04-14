// 작업지시 모듈 — 수주 품목 기반 BOM 전개, N건 작업지시 생성, 투입 자재 선점
// salesorder 완료 이벤트 수신 후 작업지시 생성, inventory 모듈로 자재 선점 요청
@ApplicationModule(allowedDependencies = {"salesorder::application", "bom::application", "inventory::application", "warehouse::application", "item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.workorder;

import org.springframework.modulith.ApplicationModule;
