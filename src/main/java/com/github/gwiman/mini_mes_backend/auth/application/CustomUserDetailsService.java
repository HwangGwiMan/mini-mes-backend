package com.github.gwiman.mini_mes_backend.auth.application;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.gwiman.mini_mes_backend.auth.domain.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security UserDetailsService 구현체.
 * common 모듈에 두면 common ↔ auth 순환 의존이 발생하므로 auth 모듈 내에 위치.
 * SecurityConfig는 UserDetailsService 인터페이스로만 주입받아 common → auth 직접 의존을 차단.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
