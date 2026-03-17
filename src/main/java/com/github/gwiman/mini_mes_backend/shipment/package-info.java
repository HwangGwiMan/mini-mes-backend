// salesorder 모듈에만 의존: SalesOrderCreatedEvent 수신 후 출하 계획 자동 생성
@ApplicationModule(allowedDependencies = {"salesorder::application", "common::domain", "common::exception", "common::util", "jooq::tables"})
package com.github.gwiman.mini_mes_backend.shipment;

import org.springframework.modulith.ApplicationModule;
