// 구매 요청 생성/승인에 필요한 마스터 도메인에 의존
// auth: 요청자 로그인 사용자 조회, employee: 요청자 조회, item: 품목 유효성 검증
@ApplicationModule(allowedDependencies = {"auth::application", "employee::application", "item::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.purchaserequest;

import org.springframework.modulith.ApplicationModule;
