// 품질검사 모듈 (Phase 4 선택) — 입고 검사, 공정 검사, 완성품 검사
// goodsreceipt(입고 검사), production(공정·완성품 검사) 이벤트와 연계
@ApplicationModule(allowedDependencies = {"goodsreceipt::application", "production::application", "item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.qualityinspection;

import org.springframework.modulith.ApplicationModule;
