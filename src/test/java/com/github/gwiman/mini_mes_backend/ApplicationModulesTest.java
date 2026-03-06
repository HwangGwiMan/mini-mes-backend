package com.github.gwiman.mini_mes_backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesTest {

    @Test
    void verifyModules() {
        ApplicationModules.of(MiniMesBackendApplication.class).verify();
    }
}
