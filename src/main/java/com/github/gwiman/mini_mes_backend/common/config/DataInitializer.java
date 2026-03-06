package com.github.gwiman.mini_mes_backend.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.auth.application.AuthService;
import com.github.gwiman.mini_mes_backend.commoncode.application.CodeGroupService;

import lombok.RequiredArgsConstructor;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final AuthService authService;
	private final CodeGroupService codeGroupService;

	@Override
	public void run(ApplicationArguments args) {
		authService.initDefaultUsers();
		codeGroupService.initDefaultCodes();
	}
}
