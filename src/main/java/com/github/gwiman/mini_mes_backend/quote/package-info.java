// 견적 생성에 필요한 핵심 마스터 도메인에 의존
// auth: 견적 담당자 로그인 사용자 조회, employee: 결재자 조회, item: 품목 유효성 검증, partner: 거래처 유효성 검증
@ApplicationModule(allowedDependencies = {"auth::application", "employee::application", "item::application", "partner::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.quote;

import org.springframework.modulith.ApplicationModule;
