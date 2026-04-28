package com.github.gwiman.mini_mes_backend.materialissue.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.Guard;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.inventory.application.InventoryService;
import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueLineRequest;
import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueRequest;
import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueResponse;
import com.github.gwiman.mini_mes_backend.materialissue.domain.MaterialIssue;
import com.github.gwiman.mini_mes_backend.materialissue.domain.MaterialIssueLine;
import com.github.gwiman.mini_mes_backend.materialissue.domain.MaterialIssueRepository;
import com.github.gwiman.mini_mes_backend.materialissue.internal.MaterialIssueQueryRepository;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrder;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrderMaterial;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrderRepository;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrderStatus;

import lombok.RequiredArgsConstructor;

/**
 * 자재 출고 애플리케이션 서비스.
 * <p>
 * 작업지시(CONFIRMED) 1건당 자재 출고 1건을 생성한다.
 * 출고 확정 시 각 라인에 대해 InventoryService.issueMaterial()을 호출하여
 * qty_on_hand와 qty_reserved를 동시에 차감한다(PRODUCTION_OUT).
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialIssueService {

	private static final String MI_NUMBER_PREFIX = "MI_";

	private final MaterialIssueRepository materialIssueRepository;
	private final MaterialIssueQueryRepository materialIssueQueryRepository;
	private final WorkOrderRepository workOrderRepository;
	private final InventoryService inventoryService;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<MaterialIssueResponse> findAll(String materialIssueNumber, String workOrderNumber, String statusCode) {
		String miPattern = QueryParamEscaper.containsLike(materialIssueNumber);
		String woPattern = QueryParamEscaper.containsLike(workOrderNumber);
		return materialIssueQueryRepository.search(miPattern, woPattern, statusCode);
	}

	public MaterialIssueResponse findById(Long id) {
		return Guard.requireFound(materialIssueQueryRepository.findById(id), "자재 출고를 찾을 수 없습니다: " + id);
	}

	/**
	 * 자재 출고 생성 — DRAFT 상태로 시작.
	 * <p>
	 * 작업지시가 CONFIRMED 상태여야 하며, 이미 출고가 존재하면 예외를 발생시킨다.
	 * WorkOrderMaterial 목록을 복사해 MaterialIssueLine을 자동 생성한다.
	 * </p>
	 */
	@Transactional
	public MaterialIssueResponse create(MaterialIssueRequest request) {
		WorkOrder wo = Guard.requireFound(
				workOrderRepository.findByIdWithMaterials(request.workOrderId()),
				"작업지시를 찾을 수 없습니다: " + request.workOrderId());

		// CONFIRMED 상태 작업지시에만 출고 생성 허용
		Guard.require(wo.getStatus() == WorkOrderStatus.CONFIRMED,
				"확정된 작업지시에만 자재 출고를 생성할 수 있습니다.");

		// 1:1 중복 체크
		Guard.require(!materialIssueRepository.existsByWorkOrderId(request.workOrderId()),
				"해당 작업지시에 대한 자재 출고가 이미 존재합니다.");

		String miNumber = documentNumberGenerator.generateRaw(MI_NUMBER_PREFIX, "material_issue", "material_issue_number");
		MaterialIssue mi = MaterialIssue.create(miNumber, request.workOrderId(),
				request.issueDate(), request.remarks());

		// WorkOrderMaterial → MaterialIssueLine 복사 (초기 출고 수량은 계획 수량)
		List<WorkOrderMaterial> materials = wo.getMaterials();
		for (WorkOrderMaterial mat : materials) {
			mi.addLine(MaterialIssueLine.of(
					mi, mat.getId(),
					mat.getMaterialItemId(), mat.getWarehouseId(),
					null, mat.getPlannedQty(), mat.getSortOrder()));
		}

		MaterialIssue saved = materialIssueRepository.save(mi);
		return Guard.requireFound(materialIssueQueryRepository.findById(saved.getId()),
				"저장된 자재 출고를 조회할 수 없습니다: " + saved.getId());
	}

	/**
	 * 자재 출고 수정 — DRAFT 상태만 허용.
	 * 라인의 LOT 및 출고 수량을 수정한다.
	 */
	@Transactional
	public MaterialIssueResponse update(Long id, MaterialIssueRequest request) {
		MaterialIssue mi = Guard.requireFound(
				materialIssueRepository.findByIdWithLines(id),
				"자재 출고를 찾을 수 없습니다: " + id);

		mi.update(request.issueDate(), request.remarks());

		// 라인 수정 — ID 매칭으로 LOT/수량 갱신
		if (request.lines() != null) {
			for (MaterialIssueLineRequest lineReq : request.lines()) {
				mi.getLines().stream()
						.filter(l -> l.getId().equals(lineReq.id()))
						.findFirst()
						.ifPresent(l -> l.update(lineReq.lotNo(), lineReq.issuedQty()));
			}
		}

		return Guard.requireFound(materialIssueQueryRepository.findById(id),
				"자재 출고를 조회할 수 없습니다: " + id);
	}

	/** 자재 출고 삭제 — DRAFT 상태만 허용 */
	@Transactional
	public void delete(Long id) {
		MaterialIssue mi = Guard.requireFound(
				materialIssueRepository.findById(id),
				"자재 출고를 찾을 수 없습니다: " + id);
		Guard.require(mi.canDelete(), "초안 상태의 자재 출고만 삭제할 수 있습니다.");
		materialIssueRepository.deleteById(id);
	}

	/**
	 * DRAFT → CONFIRMED.
	 * 각 라인에 대해 InventoryService.issueMaterial()을 호출하여 재고를 차감한다.
	 */
	@Transactional
	public void confirm(Long id) {
		MaterialIssue mi = Guard.requireFound(
				materialIssueRepository.findByIdWithLines(id),
				"자재 출고를 찾을 수 없습니다: " + id);
		mi.confirm(); // DRAFT 상태 검증 포함

		for (MaterialIssueLine line : mi.getLines()) {
			inventoryService.issueMaterial(
					line.getWarehouseId(),
					line.getMaterialItemId(),
					line.getLotNo(),
					line.getIssuedQty(),
					mi.getId());
		}
	}

	/** DRAFT / CONFIRMED → CANCELLED */
	@Transactional
	public void cancel(Long id) {
		MaterialIssue mi = Guard.requireFound(
				materialIssueRepository.findById(id),
				"자재 출고를 찾을 수 없습니다: " + id);
		mi.cancel(); // 상태 검증 포함
	}
}
