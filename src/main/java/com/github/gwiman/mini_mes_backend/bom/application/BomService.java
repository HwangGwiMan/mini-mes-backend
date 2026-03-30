package com.github.gwiman.mini_mes_backend.bom.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.bom.api.dto.BomCreateRequest;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomLineRequest;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomResponse;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomUpdateRequest;
import com.github.gwiman.mini_mes_backend.bom.domain.Bom;
import com.github.gwiman.mini_mes_backend.bom.domain.BomLine;
import com.github.gwiman.mini_mes_backend.bom.domain.BomRepository;
import com.github.gwiman.mini_mes_backend.bom.internal.BomQueryRepository;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.Guard;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;

import lombok.RequiredArgsConstructor;

/**
 * BOM 관리 서비스.
 * <p>
 * BOM 헤더 + 자재 라인의 생성/수정/복사/비활성화를 담당한다.
 * 삭제 대신 비활성 처리로 이력을 보존하며,
 * 동일 (itemId, version) 중복 등록과 순환 참조(자재가 완제품과 동일)를 방지한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BomService {

	private final BomRepository bomRepository;
	private final BomQueryRepository bomQueryRepository;
	private final ItemService itemService;

	public List<BomResponse> findAll(String itemCode, String itemName, String version, Boolean activeYn) {
		return bomQueryRepository.search(itemCode, itemName, version, activeYn);
	}

	public BomResponse findById(Long id) {
		return Guard.requireFound(bomQueryRepository.findByIdWithLines(id), "BOM을 찾을 수 없습니다: " + id);
	}

	public List<BomResponse> findByItemId(Long itemId) {
		return bomQueryRepository.findByItemId(itemId);
	}

	public boolean exists(Long id) {
		return bomRepository.existsById(id);
	}

	@Transactional
	public BomResponse create(BomCreateRequest request) {
		// 완제품 품목 존재 여부 검증
		Guard.requireExists(itemService.exists(request.itemId()), "품목을 찾을 수 없습니다: " + request.itemId());

		// 동일 (itemId, version) 중복 방지
		Guard.requireNotExists(
			bomRepository.existsByItemIdAndVersionCode(request.itemId(), request.version()),
			"동일 품목(" + request.itemId() + ")의 '" + request.version() + "' 버전 BOM이 이미 존재합니다.");

		validateLines(request.itemId(), request.lines());

		Bom bom = new Bom(request.itemId(), request.version(), request.validFrom(), request.validTo());
		addLines(bom, request.lines());

		Bom saved = bomRepository.save(bom);
		return Guard.requireFound(bomQueryRepository.findByIdWithLines(saved.getId()), "저장된 BOM을 조회할 수 없습니다: " + saved.getId());
	}

	@Transactional
	public BomResponse update(Long id, BomUpdateRequest request) {
		Bom bom = Guard.requireFound(bomRepository.findByIdWithLines(id), "BOM을 찾을 수 없습니다: " + id);

		validateLines(bom.getItemId(), request.lines());

		bom.update(request.validFrom(), request.validTo());
		bom.clearLines();
		addLines(bom, request.lines());

		return Guard.requireFound(bomQueryRepository.findByIdWithLines(id), "저장된 BOM을 조회할 수 없습니다: " + id);
	}

	@Transactional
	public BomResponse deactivate(Long id) {
		Bom bom = Guard.requireFound(bomRepository.findById(id), "BOM을 찾을 수 없습니다: " + id);
		bom.deactivate();
		return Guard.requireFound(bomQueryRepository.findByIdWithLines(id), "BOM을 찾을 수 없습니다: " + id);
	}

	/**
	 * 기존 BOM을 새 버전으로 복사한다. 헤더와 자재 라인을 그대로 복사하여 새 BOM을 생성한다.
	 */
	@Transactional
	public BomResponse copy(Long id, String newVersion) {
		Bom source = Guard.requireFound(bomRepository.findByIdWithLines(id), "BOM을 찾을 수 없습니다: " + id);

		Guard.requireNotExists(
			bomRepository.existsByItemIdAndVersionCode(source.getItemId(), newVersion),
			"동일 품목의 '" + newVersion + "' 버전 BOM이 이미 존재합니다.");

		Bom copy = new Bom(source.getItemId(), newVersion, source.getValidFrom(), source.getValidTo());
		int sortOrder = 0;
		for (BomLine sourceLine : source.getLines()) {
			copy.addLine(new BomLine(copy, sourceLine.getMaterialItemId(),
				sourceLine.getQuantity(), sourceLine.getUnit(), sourceLine.getRemarks(), sortOrder++));
		}

		Bom saved = bomRepository.save(copy);
		return Guard.requireFound(bomQueryRepository.findByIdWithLines(saved.getId()), "복사된 BOM을 조회할 수 없습니다: " + saved.getId());
	}

	/** 자재 라인의 순환 참조 및 품목 존재 여부를 검증한다. */
	private void validateLines(Long headerItemId, List<BomLineRequest> lines) {
		// 순환 참조: 자재 품목이 완제품과 동일한 경우 방지
		Guard.require(
			lines.stream().noneMatch(l -> l.materialItemId().equals(headerItemId)),
			"자재 품목에 완제품 자신을 포함할 수 없습니다.");

		// 자재 품목 존재 여부 일괄 확인 (N+1 방지)
		Set<Long> materialIds = lines.stream()
			.map(BomLineRequest::materialItemId)
			.collect(Collectors.toSet());
		Set<Long> existingIds = itemService.findExistingIds(materialIds);
		Set<Long> missingIds = materialIds.stream()
			.filter(mid -> !existingIds.contains(mid))
			.collect(Collectors.toSet());
		if (!missingIds.isEmpty()) {
			throw new ResourceNotFoundException("존재하지 않는 자재 품목 ID: " + missingIds);
		}
	}

	private void addLines(Bom bom, List<BomLineRequest> lineRequests) {
		int sortOrder = 0;
		for (BomLineRequest req : lineRequests) {
			bom.addLine(new BomLine(bom, req.materialItemId(),
				req.quantity(), req.unit(), req.remarks(), sortOrder++));
		}
	}
}
