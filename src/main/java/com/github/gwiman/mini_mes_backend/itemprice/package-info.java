/**
 * 단가 관리 모듈.
 * 품목별 기준 판매단가를 관리한다.
 */
// item 모듈에만 의존: 품목 존재 여부 및 품목 정보 조회
@ApplicationModule(allowedDependencies = {"item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.itemprice;

import org.springframework.modulith.ApplicationModule;
