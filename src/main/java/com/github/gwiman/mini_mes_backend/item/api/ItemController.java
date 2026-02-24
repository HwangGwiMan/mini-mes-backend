package com.github.gwiman.mini_mes_backend.item.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.item.api.dto.ItemRequest;
import com.github.gwiman.mini_mes_backend.item.api.dto.ItemResponse;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping("/{id}")
	public ItemResponse getById(@PathVariable Long id) {
		return itemService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ItemResponse create(@RequestBody ItemRequest request) {
		return itemService.create(request);
	}
}
