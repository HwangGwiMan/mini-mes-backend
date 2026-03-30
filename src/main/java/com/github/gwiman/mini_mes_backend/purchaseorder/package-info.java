// 구매 발주는 구매 요청 전환, 거래처, 품목 유효성 검증에 의존
@ApplicationModule(allowedDependencies = {
	"purchaserequest::application",
	"partner::application",
	"item::application",
	"common::domain",
	"common::exception",
	"common::util",
	"jooq::tables"
})
package com.github.gwiman.mini_mes_backend.purchaseorder;

import org.springframework.modulith.ApplicationModule;
