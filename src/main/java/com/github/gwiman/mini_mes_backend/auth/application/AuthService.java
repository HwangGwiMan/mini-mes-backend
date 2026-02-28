package com.github.gwiman.mini_mes_backend.auth.application;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.auth.api.dto.LoginRequest;
import com.github.gwiman.mini_mes_backend.auth.api.dto.LoginResponse;
import com.github.gwiman.mini_mes_backend.auth.api.dto.SignupRequest;
import com.github.gwiman.mini_mes_backend.auth.domain.Role;
import com.github.gwiman.mini_mes_backend.auth.domain.User;
import com.github.gwiman.mini_mes_backend.auth.domain.UserRepository;
import com.github.gwiman.mini_mes_backend.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public void signup(SignupRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + request.getUsername());
		}
		User user = new User(
			request.getUsername(),
			passwordEncoder.encode(request.getPassword()),
			Role.ROLE_USER
		);
		userRepository.save(user);
	}

	public LoginResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
			);
		} catch (AuthenticationException e) {
			throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		User user = userRepository.findByUsername(request.getUsername())
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUsername()));
		String token = jwtTokenProvider.generateToken(user);
		return new LoginResponse(token);
	}
}
