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
		createUserIfAbsent("admin",  "admin1234",  Role.ROLE_ADMIN);
		createUserIfAbsent("user01", "user1234",   Role.ROLE_USER);
	}

	private void createUserIfAbsent(String username, String rawPassword, Role role) {
		if (userRepository.existsByUsername(username)) {
			log.info("[DataInitializer] {} 계정이 이미 존재합니다.", username);
			return;
		}
		userRepository.save(new User(username, passwordEncoder.encode(rawPassword), role));
		log.info("[DataInitializer] {} 계정 생성 완료 (password: {}, role: {})", username, rawPassword, role);
	}
}
