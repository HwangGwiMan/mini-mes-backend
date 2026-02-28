package com.github.gwiman.mini_mes_backend.partner.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
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

	@GetMapping
	public List<PartnerResponse> getAll(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String name) {
		return partnerService.findAll(code, name);
	}

	@GetMapping("/{id}")
	public PartnerResponse getById(@PathVariable Long id) {
		return partnerService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PartnerResponse create(@RequestBody @Valid PartnerRequest request) {
		return partnerService.create(request);
	}

	@PutMapping("/{id}")
	public PartnerResponse update(@PathVariable Long id, @RequestBody @Valid PartnerRequest request) {
		return partnerService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		partnerService.delete(id);
	}
}
