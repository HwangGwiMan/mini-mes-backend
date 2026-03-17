// auth 모듈에만 의존: 사원 등록 시 사용자 계정(UserRepository)을 직접 연결
@ApplicationModule(allowedDependencies = {"auth::application", "auth::domain", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.employee;

import org.springframework.modulith.ApplicationModule;
