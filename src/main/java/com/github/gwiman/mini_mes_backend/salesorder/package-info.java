// quote 전환 및 수주 생성에 필요한 도메인에 의존
// quote: 견적 → 수주 전환, employee/item/partner: 마스터 데이터 검증
@ApplicationModule(allowedDependencies = {"quote::application", "employee::application", "item::application", "partner::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.salesorder;

import org.springframework.modulith.ApplicationModule;
