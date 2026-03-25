package com.github.gwiman.mini_mes_backend.bom.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.bom.api.dto.BomCopyRequest;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomCreateRequest;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomResponse;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomUpdateRequest;
import com.github.gwiman.mini_mes_backend.bom.application.BomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

	private final BomService bomService;

	@GetMapping
	public List<BomResponse> getAll(
		@RequestParam(required = false) String itemCode,
		@RequestParam(required = false) String itemName,
		@RequestParam(required = false) String version,
		@RequestParam(required = false) Boolean activeYn) {
		return bomService.findAll(itemCode, itemName, version, activeYn);
	}

	@GetMapping("/{id}")
	public BomResponse getById(@PathVariable Long id) {
		return bomService.findById(id);
	}

	@GetMapping("/by-item/{itemId}")
	public List<BomResponse> getByItemId(@PathVariable Long itemId) {
		return bomService.findByItemId(itemId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BomResponse create(@RequestBody @Valid BomCreateRequest request) {
		return bomService.create(request);
	}

	@PutMapping("/{id}")
	public BomResponse update(@PathVariable Long id, @RequestBody @Valid BomUpdateRequest request) {
		return bomService.update(id, request);
	}

	@PatchMapping("/{id}/deactivate")
	public BomResponse deactivate(@PathVariable Long id) {
		return bomService.deactivate(id);
	}

	@PostMapping("/{id}/copy")
	@ResponseStatus(HttpStatus.CREATED)
	public BomResponse copy(@PathVariable Long id, @RequestBody @Valid BomCopyRequest request) {
		return bomService.copy(id, request.getNewVersion());
	}
}
