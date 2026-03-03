package com.github.gwiman.mini_mes_backend.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.auth.domain.Role;
import com.github.gwiman.mini_mes_backend.auth.domain.User;
import com.github.gwiman.mini_mes_backend.auth.domain.UserRepository;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroup;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroupRepository;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCode;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CodeGroupRepository codeGroupRepository;
	private final CommonCodeRepository commonCodeRepository;

	@Override
	public void run(ApplicationArguments args) {
		createUserIfAbsent("admin",  "admin1234",  Role.ROLE_ADMIN);
		createUserIfAbsent("user01", "user1234",   Role.ROLE_USER);

		createCodeGroupIfAbsent("TRADE_TYPE",   "거래구분", 1);
		createCommonCodeIfAbsent("TRADE_TYPE", "TRADE_TYPE_01", "매입처",      1);
		createCommonCodeIfAbsent("TRADE_TYPE", "TRADE_TYPE_02", "매출처",      2);
		createCommonCodeIfAbsent("TRADE_TYPE", "TRADE_TYPE_03", "매입/매출처", 3);

		createCodeGroupIfAbsent("PROCESS_TYPE", "공정유형", 2);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_01", "가공", 1);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_02", "조립", 2);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_03", "검사", 3);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_04", "포장", 4);

		createCodeGroupIfAbsent("DEPT", "부서", 3);
		createCommonCodeIfAbsent("DEPT", "DEPT_01", "생산1부", 1);
		createCommonCodeIfAbsent("DEPT", "DEPT_02", "생산2부", 2);
		createCommonCodeIfAbsent("DEPT", "DEPT_03", "품질부", 3);
		createCommonCodeIfAbsent("DEPT", "DEPT_04", "관리부", 4);

		createCodeGroupIfAbsent("POSITION", "직급", 4);
		createCommonCodeIfAbsent("POSITION", "POSITION_01", "사원", 1);
		createCommonCodeIfAbsent("POSITION", "POSITION_02", "대리", 2);
		createCommonCodeIfAbsent("POSITION", "POSITION_03", "과장", 3);
		createCommonCodeIfAbsent("POSITION", "POSITION_04", "부장", 4);

		createCodeGroupIfAbsent("ITEM_TYPE", "품목유형", 5);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_01", "원자재", 1);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_02", "반제품", 2);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_03", "완제품", 3);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_04", "부자재", 4);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_05", "상품", 5);

		createCodeGroupIfAbsent("UNIT", "단위", 6);
		createCommonCodeIfAbsent("UNIT", "UNIT_01", "EA", 1);
		createCommonCodeIfAbsent("UNIT", "UNIT_02", "KG", 2);
		createCommonCodeIfAbsent("UNIT", "UNIT_03", "M", 3);
		createCommonCodeIfAbsent("UNIT", "UNIT_04", "L", 4);
		createCommonCodeIfAbsent("UNIT", "UNIT_05", "BOX", 5);
		createCommonCodeIfAbsent("UNIT", "UNIT_06", "PCS", 6);
	}

	private void createUserIfAbsent(String username, String rawPassword, Role role) {
		if (userRepository.existsByUsername(username)) {
			log.info("[DataInitializer] {} 계정이 이미 존재합니다.", username);
			return;
		}
		userRepository.save(new User(username, passwordEncoder.encode(rawPassword), role));
		log.info("[DataInitializer] {} 계정 생성 완료 (password: {}, role: {})", username, rawPassword, role);
	}

	private void createCodeGroupIfAbsent(String groupCode, String groupName, int sortOrder) {
		if (codeGroupRepository.existsByGroupCode(groupCode)) {
			log.info("[DataInitializer] 그룹코드 [{}] 이미 존재합니다.", groupCode);
			return;
		}
		codeGroupRepository.save(new CodeGroup(groupCode, groupName, sortOrder));
		log.info("[DataInitializer] 그룹코드 [{}] {} 생성 완료", groupCode, groupName);
	}

	private void createCommonCodeIfAbsent(String codeGroup, String code, String name, int sortOrder) {
		boolean exists = commonCodeRepository.findByCodeGroupAndUseYnTrueOrderBySortOrder(codeGroup)
			.stream().anyMatch(c -> c.getCode().equals(code));
		if (exists) {
			log.info("[DataInitializer] 공통코드 [{}/{}] 이미 존재합니다.", codeGroup, code);
			return;
		}
		commonCodeRepository.save(new CommonCode(codeGroup, code, name, sortOrder));
		log.info("[DataInitializer] 공통코드 [{}/{}] {} 생성 완료", codeGroup, code, name);
	}
}
