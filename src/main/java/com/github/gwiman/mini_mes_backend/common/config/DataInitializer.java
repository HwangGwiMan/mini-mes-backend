package com.github.gwiman.mini_mes_backend.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.auth.application.AuthService;
import com.github.gwiman.mini_mes_backend.commoncode.application.CodeGroupService;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;

import lombok.RequiredArgsConstructor;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final AuthService authService;
	private final CodeGroupService codeGroupService;
	private final PartnerService partnerService;
	private final EmployeeService employeeService;
	private final ItemService itemService;

	@Override
	public void run(ApplicationArguments args) {
		// 공통코드 및 계정 먼저 — 거래처·사원·품목이 코드값을 참조하므로 선행 필요
		authService.initDefaultUsers();
		codeGroupService.initDefaultCodes();
		partnerService.initDefaultPartners();
		employeeService.initDefaultEmployees();
		itemService.initDefaultItems();
	}
}
