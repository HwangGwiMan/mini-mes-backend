// 다른 도메인 모듈에 의존하지 않는 독립 모듈
@ApplicationModule(allowedDependencies = {"common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.partner;

import org.springframework.modulith.ApplicationModule;
