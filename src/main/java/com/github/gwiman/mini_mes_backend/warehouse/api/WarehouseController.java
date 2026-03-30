package com.github.gwiman.mini_mes_backend.warehouse.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.warehouse.api.dto.WarehouseRequest;
import com.github.gwiman.mini_mes_backend.warehouse.api.dto.WarehouseResponse;
import com.github.gwiman.mini_mes_backend.warehouse.application.WarehouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

	private final WarehouseService warehouseService;

	@GetMapping
	public List<WarehouseResponse> getAll(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String name,
		@RequestParam(required = false) Boolean useYn) {
		return warehouseService.findAll(code, name, useYn);
	}

	@GetMapping("/{id}")
	public WarehouseResponse getById(@PathVariable Long id) {
		return warehouseService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WarehouseResponse create(@RequestBody @Valid WarehouseRequest request) {
		return warehouseService.create(request);
	}

	@PutMapping("/{id}")
	public WarehouseResponse update(@PathVariable Long id, @RequestBody @Valid WarehouseRequest request) {
		return warehouseService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		warehouseService.delete(id);
	}
}
