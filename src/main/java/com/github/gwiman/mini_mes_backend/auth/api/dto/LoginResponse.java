package com.github.gwiman.mini_mes_backend.auth.api.dto;

public record LoginResponse(String accessToken, String tokenType) {

	public LoginResponse(String accessToken) {
		this(accessToken, "Bearer");
	}
}
