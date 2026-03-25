// BOM(자재명세서) 도메인: 완제품 1개 생산에 필요한 자재 목록을 버전 단위로 관리
// item: 완제품·자재 품목 존재 여부 검증
@ApplicationModule(allowedDependencies = {"item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.bom;

import org.springframework.modulith.ApplicationModule;
