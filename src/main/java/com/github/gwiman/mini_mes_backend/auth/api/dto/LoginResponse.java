package com.github.gwiman.mini_mes_backend.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

	private String accessToken;
	private String tokenType = "Bearer";

	public LoginResponse(String accessToken) {
		this.accessToken = accessToken;
	}
}
