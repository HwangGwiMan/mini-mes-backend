package com.github.gwiman.mini_mes_backend.routing.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.bom.application.BomService;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.Guard;
import com.github.gwiman.mini_mes_backend.process.application.ProcessService;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingCreateRequest;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingResponse;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingStepRequest;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingUpdateRequest;
import com.github.gwiman.mini_mes_backend.routing.domain.Routing;
import com.github.gwiman.mini_mes_backend.routing.domain.RoutingRepository;
import com.github.gwiman.mini_mes_backend.routing.domain.RoutingStep;
import com.github.gwiman.mini_mes_backend.routing.internal.RoutingQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 라우팅 관리 서비스.
 * <p>
 * BOM별 공정 순서의 생성/수정/비활성화를 담당한다.
 * BOM 1개에 라우팅 1개만 허용하며(ADR-002), 동일 라우팅 내 중복 공정과 존재하지 않는 공정 참조를 방지한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutingService {

	private final RoutingRepository routingRepository;
	private final RoutingQueryRepository routingQueryRepository;
	private final BomService bomService;
	private final ProcessService processService;

	public List<RoutingResponse> findAll(String itemCode, String itemName, String bomVersion, Boolean activeYn) {
		return routingQueryRepository.search(itemCode, itemName, bomVersion, activeYn);
	}

	public RoutingResponse findById(Long id) {
		return Guard.requireFound(routingQueryRepository.findByIdWithSteps(id), "라우팅을 찾을 수 없습니다: " + id);
	}

	public Optional<RoutingResponse> findByBomId(Long bomId) {
		return routingQueryRepository.findByBomId(bomId);
	}

	/**
	 * BOM ID로 활성 라우팅의 공정 단계 목록을 반환한다.
	 * 타 모듈(workorder)에서 라우팅 전개 시 사용 — 라우팅이 없거나 비활성이면 빈 리스트 반환.
	 */
	public List<RoutingStepData> findStepsByBomId(Long bomId) {
		return routingRepository.findByBomIdWithSteps(bomId)
			.filter(r -> Boolean.TRUE.equals(r.getActiveYn()))
			.map(r -> r.getSteps().stream()
				.map(s -> new RoutingStepData(r.getId(), s.getProcessId(),
					s.getStepOrder(), s.getStandardTime(), s.getRemarks()))
				.toList())
			.orElse(List.of());
	}

	public record RoutingStepData(Long routingId, Long processId,
		int stepOrder, Integer standardTime, String remarks) {
	}

	public List<RoutingResponse> findByItemId(Long itemId) {
		return routingQueryRepository.findByItemId(itemId);
	}

	@Transactional
	public RoutingResponse create(RoutingCreateRequest request) {
		// BOM 존재 여부 검증
		Guard.requireExists(bomService.exists(request.bomId()), "BOM을 찾을 수 없습니다: " + request.bomId());

		// BOM당 라우팅 1개 제한
		Guard.requireNotExists(
			routingRepository.existsByBomId(request.bomId()),
			"해당 BOM에 이미 라우팅이 존재합니다: " + request.bomId());

		validateSteps(request.steps());

		Routing routing = new Routing(request.bomId());
		addSteps(routing, request.steps());

		Routing saved = routingRepository.save(routing);
		return Guard.requireFound(routingQueryRepository.findByIdWithSteps(saved.getId()), "저장된 라우팅을 조회할 수 없습니다: " + saved.getId());
	}

	@Transactional
	public RoutingResponse update(Long id, RoutingUpdateRequest request) {
		Routing routing = Guard.requireFound(routingRepository.findByIdWithSteps(id), "라우팅을 찾을 수 없습니다: " + id);

		validateSteps(request.steps());

		routing.clearSteps();
		addSteps(routing, request.steps());

		return Guard.requireFound(routingQueryRepository.findByIdWithSteps(id), "저장된 라우팅을 조회할 수 없습니다: " + id);
	}

	@Transactional
	public RoutingResponse deactivate(Long id) {
		Routing routing = Guard.requireFound(routingRepository.findById(id), "라우팅을 찾을 수 없습니다: " + id);
		routing.deactivate();
		return Guard.requireFound(routingQueryRepository.findByIdWithSteps(id), "라우팅을 찾을 수 없습니다: " + id);
	}

	/** 공정 단계의 중복 공정 및 존재 여부를 검증한다. */
	private void validateSteps(List<RoutingStepRequest> steps) {
		// 동일 라우팅 내 공정 중복 방지
		Set<Long> processIds = steps.stream()
			.map(RoutingStepRequest::processId)
			.collect(Collectors.toSet());
		Guard.require(
			processIds.size() == steps.size(),
			"동일한 공정을 라우팅에 중복 등록할 수 없습니다.");

		// 공정 존재 여부 일괄 확인 (N+1 방지)
		Set<Long> existingIds = processService.findExistingIds(processIds);
		Set<Long> missingIds = processIds.stream()
			.filter(pid -> !existingIds.contains(pid))
			.collect(Collectors.toSet());
		if (!missingIds.isEmpty()) {
			throw new ResourceNotFoundException("존재하지 않는 공정 ID: " + missingIds);
		}
	}

	private void addSteps(Routing routing, List<RoutingStepRequest> stepRequests) {
		for (RoutingStepRequest req : stepRequests) {
			routing.addStep(new RoutingStep(
				routing, req.processId(), req.stepOrder(), req.standardTime(), req.remarks()));
		}
	}
}
