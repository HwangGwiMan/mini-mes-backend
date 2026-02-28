package com.github.gwiman.mini_mes_backend.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.auth.domain.Role;
import com.github.gwiman.mini_mes_backend.auth.domain.User;
import com.github.gwiman.mini_mes_backend.auth.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(ApplicationArguments args) {
		createAdminIfAbsent();
	}

	private void createAdminIfAbsent() {
		if (userRepository.existsByUsername("admin")) {
			log.info("[DataInitializer] admin 계정이 이미 존재합니다.");
			return;
		}
		User admin = new User("admin", passwordEncoder.encode("admin1234"), Role.ROLE_ADMIN);
		userRepository.save(admin);
		log.info("[DataInitializer] admin 계정 생성 완료 (username: admin / password: admin1234)");
	}
}
