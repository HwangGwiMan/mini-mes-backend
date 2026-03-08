package com.github.gwiman.mini_mes_backend.price.api;

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

import com.github.gwiman.mini_mes_backend.price.api.dto.ItemPriceRequest;
import com.github.gwiman.mini_mes_backend.price.api.dto.ItemPriceResponse;
import com.github.gwiman.mini_mes_backend.price.application.ItemPriceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/item-prices")
@RequiredArgsConstructor
public class ItemPriceController {

	private final ItemPriceService itemPriceService;

	@GetMapping
	public List<ItemPriceResponse> getAll(
		@RequestParam(required = false) String itemCode,
		@RequestParam(required = false) String itemName) {
		return itemPriceService.findAll(itemCode, itemName);
	}

	@GetMapping("/{id}")
	public ItemPriceResponse getById(@PathVariable Long id) {
		return itemPriceService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ItemPriceResponse create(@RequestBody @Valid ItemPriceRequest request) {
		return itemPriceService.create(request);
	}

	@PutMapping("/{id}")
	public ItemPriceResponse update(@PathVariable Long id, @RequestBody @Valid ItemPriceRequest request) {
		return itemPriceService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		itemPriceService.delete(id);
	}
}
