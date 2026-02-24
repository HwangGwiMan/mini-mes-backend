package com.github.gwiman.mini_mes_backend.commoncode.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeRequest;
import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeResponse;
import com.github.gwiman.mini_mes_backend.commoncode.application.CommonCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {

	private final CommonCodeService commonCodeService;

	@GetMapping("/{id}")
	public CommonCodeResponse getById(@PathVariable Long id) {
		return commonCodeService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CommonCodeResponse create(@RequestBody CommonCodeRequest request) {
		return commonCodeService.create(request);
	}
}
