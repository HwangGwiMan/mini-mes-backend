// 다른 도메인 모듈에 의존하지 않는 독립 모듈
@ApplicationModule(allowedDependencies = {"common::domain", "common::security", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.auth;

import org.springframework.modulith.ApplicationModule;
