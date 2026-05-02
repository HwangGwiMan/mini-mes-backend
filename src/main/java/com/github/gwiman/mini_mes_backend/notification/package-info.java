// 설계 결정 근거: doc/docs/adr/007-notification-sse-design.md (SSE + DB 테이블 방식 채택)
// notification 모듈은 도메인 이벤트를 수신만 하고 다른 모듈에 의존되지 않아 순환 의존이 없다.
@ApplicationModule(allowedDependencies = {
	"quote::application",
	"purchaserequest::application",
	"purchaseorder::application",
	"employee::application",
	"auth::application",
	"common::domain",
	"common::exception",
	"common::security",
	"common::util"
})
package com.github.gwiman.mini_mes_backend.notification;

import org.springframework.modulith.ApplicationModule;
