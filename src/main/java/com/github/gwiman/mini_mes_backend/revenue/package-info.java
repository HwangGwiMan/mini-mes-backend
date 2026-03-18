// salesorder 모듈에만 의존: 거래처의 완료 수주 라인을 조회해 수동으로 매출을 생성
@ApplicationModule(allowedDependencies = {"salesorder::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.revenue;

import org.springframework.modulith.ApplicationModule;
