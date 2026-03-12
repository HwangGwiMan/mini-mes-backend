package com.github.gwiman.mini_mes_backend.item.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.item.api.dto.ItemRequest;
import com.github.gwiman.mini_mes_backend.item.api.dto.ItemResponse;
import com.github.gwiman.mini_mes_backend.item.domain.Item;
import com.github.gwiman.mini_mes_backend.item.domain.ItemRepository;
import com.github.gwiman.mini_mes_backend.item.internal.ItemQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

	private final ItemRepository itemRepository;
	private final ItemQueryRepository itemQueryRepository;

	public List<ItemResponse> findAll(String code, String name) {
		return itemRepository.search(QueryParamEscaper.escapeLike(code), QueryParamEscaper.escapeLike(name)).stream()
			.map(ItemResponse::from)
			.toList();
	}

	public ItemResponse findById(Long id) {
		return itemQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("품목을 찾을 수 없습니다: " + id));
	}

	@Transactional
	public ItemResponse create(ItemRequest request) {
		Item entity = new Item(
			request.getCode(),
			request.getName(),
			request.getItemTypeCode(),
			request.getUnit(),
			request.getSpec(),
			request.getDescription(),
			request.isUseYn(),
			request.getSortOrder()
		);
		return ItemResponse.from(itemRepository.save(entity));
	}

	@Transactional
	public ItemResponse update(Long id, ItemRequest request) {
		Item entity = itemRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("품목을 찾을 수 없습니다: " + id));
		entity.update(
			request.getCode(),
			request.getName(),
			request.getItemTypeCode(),
			request.getUnit(),
			request.getSpec(),
			request.getDescription(),
			request.isUseYn(),
			request.getSortOrder()
		);
		return ItemResponse.from(entity);
	}

	public boolean exists(Long id) {
		return itemRepository.existsById(id);
	}

	@Transactional
	public void delete(Long id) {
		if (!itemRepository.existsById(id)) {
			throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + id);
		}
		itemRepository.deleteById(id);
	}

	@Transactional
	public void initDefaultItems() {
		createItemIfAbsent("M001", "냉연강판 1.0T",  "ITEM_TYPE_01", "KG",   "1.0T x 1219 x 2438", null, 1);
		createItemIfAbsent("M002", "알루미늄봉 Φ20", "ITEM_TYPE_01", "M",    "Φ20 6061-T6",         null, 2);
		createItemIfAbsent("S001", "볼트 M10x30",    "ITEM_TYPE_04", "PCS",  "M10x30 SUS304",       null, 3);
		createItemIfAbsent("P001", "하우징 어셈블리", "ITEM_TYPE_02", "EA",   null,                  null, 4);
		createItemIfAbsent("F001", "전동 액추에이터", "ITEM_TYPE_03", "EA",   "24V DC, 스트로크 100mm", null, 5);
		createItemIfAbsent("F002", "산업용 펌프 A형", "ITEM_TYPE_03", "EA",   "유량 200L/min",        null, 6);
		createItemIfAbsent("F003", "제어 패널 유닛",  "ITEM_TYPE_03", "EA",   "PLC 내장형",           null, 7);
	}

	private void createItemIfAbsent(String code, String name, String itemTypeCode,
		String unit, String spec, String description, int sortOrder) {
		if (!itemRepository.existsByCode(code)) {
			itemRepository.save(new Item(code, name, itemTypeCode, unit, spec, description, true, sortOrder));
		}
	}
}
