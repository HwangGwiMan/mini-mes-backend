package com.github.gwiman.mini_mes_backend.auth.api;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.github.gwiman.mini_mes_backend.auth.api.dto.CurrentUserDto;
import com.github.gwiman.mini_mes_backend.auth.domain.User;

@Controller
public class AuthGraphqlController {

	@QueryMapping
	public CurrentUserDto me() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return new CurrentUserDto(user.getUsername(), user.getRole().name());
	}
}
