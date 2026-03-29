// 라우팅(공정순서) 도메인: BOM별 생산에 필요한 공정 순서를 단계별로 관리 (ADR-002: BOM 직접 참조)
// bom: BOM 존재 여부 검증, process: 공정 존재 여부 검증
@ApplicationModule(allowedDependencies = {"bom::application", "process::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.routing;

import org.springframework.modulith.ApplicationModule;
