// 창고 기준정보 도메인 — 재고 관리의 기본 단위, Phase 2 재고 도메인의 선행 필수 조건
// 다른 도메인 모듈에 의존하지 않는 독립 모듈
@ApplicationModule(allowedDependencies = {"common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.warehouse;

import org.springframework.modulith.ApplicationModule;
