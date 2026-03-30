package com.github.gwiman.mini_mes_backend.warehouse.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.warehouse.api.dto.WarehouseRequest;
import com.github.gwiman.mini_mes_backend.warehouse.api.dto.WarehouseResponse;
import com.github.gwiman.mini_mes_backend.warehouse.domain.Warehouse;
import com.github.gwiman.mini_mes_backend.warehouse.domain.WarehouseRepository;
import com.github.gwiman.mini_mes_backend.warehouse.internal.WarehouseQueryRepository;

import lombok.RequiredArgsConstructor;

/** 창고 기준정보 서비스 — Phase 2 재고 도메인에서 warehouseService.exists() 참조 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

	private final WarehouseRepository warehouseRepository;
	private final WarehouseQueryRepository warehouseQueryRepository;

	public List<WarehouseResponse> findAll(String code, String name, Boolean useYn) {
		return warehouseRepository
			.search(QueryParamEscaper.escapeLike(code), QueryParamEscaper.escapeLike(name), useYn)
			.stream()
			.map(WarehouseResponse::from)
			.toList();
	}

	public WarehouseResponse findById(Long id) {
		return warehouseQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("창고를 찾을 수 없습니다: " + id));
	}

	@Transactional
	public WarehouseResponse create(WarehouseRequest request) {
		if (warehouseRepository.existsByCode(request.code())) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.code());
		}
		Warehouse entity = new Warehouse(
			request.code(),
			request.name(),
			request.warehouseTypeCode(),
			request.description(),
			request.useYn(),
			request.sortOrder()
		);
		return WarehouseResponse.from(warehouseRepository.save(entity));
	}

	@Transactional
	public WarehouseResponse update(Long id, WarehouseRequest request) {
		Warehouse entity = warehouseRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("창고를 찾을 수 없습니다: " + id));
		if (warehouseRepository.existsByCodeAndIdNot(request.code(), id)) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.code());
		}
		entity.update(
			request.code(),
			request.name(),
			request.warehouseTypeCode(),
			request.description(),
			request.useYn(),
			request.sortOrder()
		);
		return WarehouseResponse.from(entity);
	}

	@Transactional
	public void delete(Long id) {
		// Phase 2에서 재고 존재 여부 Guard 추가 예정
		if (!warehouseRepository.existsById(id)) {
			throw new ResourceNotFoundException("창고를 찾을 수 없습니다: " + id);
		}
		warehouseRepository.deleteById(id);
	}

	/** 재고 도메인 등 타 도메인에서 창고 존재 여부 검증용 */
	public boolean exists(Long id) {
		return warehouseRepository.existsById(id);
	}
}
