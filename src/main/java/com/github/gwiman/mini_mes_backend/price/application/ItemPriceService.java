package com.github.gwiman.mini_mes_backend.price.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.price.api.dto.ItemPriceRequest;
import com.github.gwiman.mini_mes_backend.price.api.dto.ItemPriceResponse;
import com.github.gwiman.mini_mes_backend.price.domain.ItemPrice;
import com.github.gwiman.mini_mes_backend.price.domain.ItemPriceRepository;
import com.github.gwiman.mini_mes_backend.price.internal.ItemPriceQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 품목별 기준 판매단가 서비스.
 * 품목당 단가는 1개만 허용하며, 중복 등록 시 예외를 발생시킨다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemPriceService {

	private final ItemPriceRepository itemPriceRepository;
	private final ItemPriceQueryRepository itemPriceQueryRepository;
	private final ItemService itemService;

	public List<ItemPriceResponse> findAll(String itemCode, String itemName) {
		String codePattern = QueryParamEscaper.containsLike(itemCode);
		String namePattern = QueryParamEscaper.containsLike(itemName);
		return itemPriceQueryRepository.search(codePattern, namePattern);
	}

	public ItemPriceResponse findById(Long id) {
		return itemPriceQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("단가를 찾을 수 없습니다: " + id));
	}

	@Transactional
	public ItemPriceResponse create(ItemPriceRequest request) {
		if (!itemService.exists(request.getItemId())) {
			throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + request.getItemId());
		}
		// 품목당 단가 1개 보장
		if (itemPriceRepository.existsByItemId(request.getItemId())) {
			throw new BusinessRuleViolationException("이미 단가가 등록된 품목입니다.");
		}

		ItemPrice saved = itemPriceRepository.save(
			new ItemPrice(request.getItemId(), request.getUnitPrice(), request.getRemarks())
		);
		return itemPriceQueryRepository.findById(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 단가를 조회할 수 없습니다: " + saved.getId()));
	}

	@Transactional
	public ItemPriceResponse update(Long id, ItemPriceRequest request) {
		ItemPrice entity = itemPriceRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("단가를 찾을 수 없습니다: " + id));

		entity.update(request.getUnitPrice(), request.getRemarks());
		return itemPriceQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 단가를 조회할 수 없습니다: " + id));
	}

	@Transactional
	public void delete(Long id) {
		if (!itemPriceRepository.existsById(id)) {
			throw new ResourceNotFoundException("단가를 찾을 수 없습니다: " + id);
		}
		itemPriceRepository.deleteById(id);
	}

}
