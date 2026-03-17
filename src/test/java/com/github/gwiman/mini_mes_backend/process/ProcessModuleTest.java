package com.github.gwiman.mini_mes_backend.process;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * process 모듈 격리 테스트.
 * STANDALONE 모드로 process 모듈 Bean만 로드하며,
 * 컨텍스트 로드 실패 시 모듈 경계 위반을 의미한다.
 */
@ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.STANDALONE)
class ProcessModuleTest {

    @Test
    void contextLoads() {
        // 모듈 컨텍스트가 정상적으로 로드되면 경계가 올바르게 설정된 것이다.
    }
}
