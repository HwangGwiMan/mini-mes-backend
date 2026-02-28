package com.github.gwiman.mini_mes_backend.item.application;

import java.util.List;

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

	public List<ItemResponse> findAll(String code, String name) {
		return itemRepository.search(escapeLike(code), escapeLike(name)).stream()
			.map(ItemResponse::from)
			.toList();
	}

	private String escapeLike(String value) {
		if (value == null || value.isBlank()) return null;
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	public ItemResponse findById(Long id) {
		Item entity = itemRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다: " + id));
		return ItemResponse.from(entity);
	}

	@Transactional
	public ItemResponse create(ItemRequest request) {
		Item entity = new Item(request.getCode(), request.getName());
		return ItemResponse.from(itemRepository.save(entity));
	}

	@Transactional
	public ItemResponse update(Long id, ItemRequest request) {
		Item entity = itemRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다: " + id));
		entity.update(request.getCode(), request.getName());
		return ItemResponse.from(entity);
	}

	@Transactional
	public void delete(Long id) {
		if (!itemRepository.existsById(id)) {
			throw new IllegalArgumentException("품목을 찾을 수 없습니다: " + id);
		}
		itemRepository.deleteById(id);
	}
}
