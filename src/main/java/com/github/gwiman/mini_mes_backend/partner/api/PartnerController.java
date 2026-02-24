package com.github.gwiman.mini_mes_backend.partner.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerRequest;
import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerResponse;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class PartnerController {

	private final PartnerService partnerService;

	@GetMapping("/{id}")
	public PartnerResponse getById(@PathVariable Long id) {
		return partnerService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PartnerResponse create(@RequestBody PartnerRequest request) {
		return partnerService.create(request);
	}
}
