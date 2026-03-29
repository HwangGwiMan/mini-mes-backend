package com.github.gwiman.mini_mes_backend.routing.api;

import java.util.List;
import java.util.Optional;

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

import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingCreateRequest;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingResponse;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingUpdateRequest;
import com.github.gwiman.mini_mes_backend.routing.application.RoutingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routings")
@RequiredArgsConstructor
public class RoutingController {

	private final RoutingService routingService;

	@GetMapping
	public List<RoutingResponse> getAll(
		@RequestParam(required = false) String itemCode,
		@RequestParam(required = false) String itemName,
		@RequestParam(required = false) String bomVersion,
		@RequestParam(required = false) Boolean activeYn) {
		return routingService.findAll(itemCode, itemName, bomVersion, activeYn);
	}

	@GetMapping("/{id}")
	public RoutingResponse getById(@PathVariable Long id) {
		return routingService.findById(id);
	}

	@GetMapping("/by-bom/{bomId}")
	public Optional<RoutingResponse> getByBomId(@PathVariable Long bomId) {
		return routingService.findByBomId(bomId);
	}

	@GetMapping("/by-item/{itemId}")
	public List<RoutingResponse> getByItemId(@PathVariable Long itemId) {
		return routingService.findByItemId(itemId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RoutingResponse create(@RequestBody @Valid RoutingCreateRequest request) {
		return routingService.create(request);
	}

	@PutMapping("/{id}")
	public RoutingResponse update(@PathVariable Long id, @RequestBody @Valid RoutingUpdateRequest request) {
		return routingService.update(id, request);
	}

	@PatchMapping("/{id}/deactivate")
	public RoutingResponse deactivate(@PathVariable Long id) {
		return routingService.deactivate(id);
	}
}
