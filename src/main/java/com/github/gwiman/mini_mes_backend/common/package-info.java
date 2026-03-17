// 모든 모듈이 공유하는 횡단 관심사(BaseEntity, 예외, 유틸리티, 보안)
// 다른 도메인 모듈에 의존하지 않는 독립 모듈
@ApplicationModule(allowedDependencies = {})
package com.github.gwiman.mini_mes_backend.common;

import org.springframework.modulith.ApplicationModule;
