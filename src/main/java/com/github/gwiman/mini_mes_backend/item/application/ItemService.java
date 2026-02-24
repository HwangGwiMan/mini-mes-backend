package com.github.gwiman.mini_mes_backend.item.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.item.api.dto.ItemRequest;
import com.github.gwiman.mini_mes_backend.item.api.dto.ItemResponse;
import com.github.gwiman.mini_mes_backend.item.domain.Item;
import com.github.gwiman.mini_mes_backend.item.domain.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

	private final ItemRepository itemRepository;

	public ItemResponse findById(Long id) {
		Item entity = itemRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
		return ItemResponse.from(entity);
	}

	@Transactional
	public ItemResponse create(ItemRequest request) {
		Item entity = new Item(request.getCode(), request.getName());
		Item saved = itemRepository.save(entity);
		return ItemResponse.from(saved);
	}
}
