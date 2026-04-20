package com.github.gwiman.mini_mes_backend.workorder.application;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.bom.api.dto.BomLineResponse;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomResponse;
import com.github.gwiman.mini_mes_backend.bom.application.BomService;
import com.github.gwiman.mini_mes_backend.routing.application.RoutingService;
import com.github.gwiman.mini_mes_backend.routing.application.RoutingService.RoutingStepData;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.Guard;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.inventory.application.InventoryService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.salesorder.application.SalesOrderService;
import com.github.gwiman.mini_mes_backend.warehouse.application.WarehouseService;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderRequest;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderResponse;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrder;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrderMaterial;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrderRepository;
import com.github.gwiman.mini_mes_backend.workorder.domain.WorkOrderRouting;
import com.github.gwiman.mini_mes_backend.workorder.internal.WorkOrderQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 작업지시 애플리케이션 서비스.
 * <p>
 * 수주 품목 기반 BOM 전개, 작업지시 생성, 투입 자재 선점을 담당한다.
 * - 생성: BOM 전개 → WorkOrderMaterial 스냅샷 생성
 * - 확정: InventoryService.reserveMaterial() 호출 (자재 선점)
 * - 취소: CONFIRMED 상태이면 InventoryService.unreserveMaterial() 호출 (선점 해제)
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkOrderService {

	private static final String WO_NUMBER_PREFIX = "WO_";

	private final WorkOrderRepository workOrderRepository;
	private final WorkOrderQueryRepository workOrderQueryRepository;
	private final BomService bomService;
	private final RoutingService routingService;
	private final InventoryService inventoryService;
	private final ItemService itemService;
	private final WarehouseService warehouseService;
	private final SalesOrderService salesOrderService;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<WorkOrderResponse> findAll(String workOrderNumber, String itemName, String statusCode) {
		String woPattern   = QueryParamEscaper.containsLike(workOrderNumber);
		String itemPattern = QueryParamEscaper.containsLike(itemName);
		return workOrderQueryRepository.search(woPattern, itemPattern, statusCode);
	}

	public WorkOrderResponse findById(Long id) {
		return Guard.requireFound(workOrderQueryRepository.findById(id), "작업지시를 찾을 수 없습니다: " + id);
	}

	@Transactional
	public WorkOrderResponse create(WorkOrderRequest request) {
		validateRequest(request);

		BomResponse bom = bomService.findById(request.bomId());
		Guard.require(Boolean.TRUE.equals(bom.activeYn()), "비활성 BOM으로는 작업지시를 생성할 수 없습니다.");

		String workOrderNumber = documentNumberGenerator.generateRaw(WO_NUMBER_PREFIX, "work_order", "work_order_number");
		WorkOrder wo = WorkOrder.create(
				workOrderNumber,
				request.salesOrderId(), request.salesOrderLineId(),
				request.itemId(), request.bomId(), request.warehouseId(),
				request.plannedQty(),
				request.plannedStartDate(), request.plannedEndDate(),
				request.remarks());

		// BOM 전개 → WorkOrderMaterial 스냅샷 생성
		expandBomLines(wo, bom.lines(), request.plannedQty(), request.warehouseId());
		// 라우팅 전개 → WorkOrderRouting 스냅샷 생성 (라우팅 없으면 스킵)
		expandRoutingSteps(wo, request.bomId());

		WorkOrder saved = workOrderRepository.save(wo);
		return Guard.requireFound(workOrderQueryRepository.findById(saved.getId()), "저장된 작업지시를 조회할 수 없습니다: " + saved.getId());
	}

	@Transactional
	public WorkOrderResponse update(Long id, WorkOrderRequest request) {
		WorkOrder wo = Guard.requireFound(workOrderRepository.findByIdWithMaterials(id), "작업지시를 찾을 수 없습니다: " + id);
		validateRequest(request);

		BomResponse bom = bomService.findById(request.bomId());
		Guard.require(Boolean.TRUE.equals(bom.activeYn()), "비활성 BOM으로는 작업지시를 수정할 수 없습니다.");

		wo.update(request.salesOrderId(), request.salesOrderLineId(),
				request.itemId(), request.bomId(), request.warehouseId(),
				request.plannedQty(),
				request.plannedStartDate(), request.plannedEndDate(),
				request.remarks());

		// 자재·라우팅 목록 재생성
		wo.clearMaterials();
		expandBomLines(wo, bom.lines(), request.plannedQty(), request.warehouseId());
		wo.clearRoutings();
		expandRoutingSteps(wo, request.bomId());

		return Guard.requireFound(workOrderQueryRepository.findById(id), "저장된 작업지시를 조회할 수 없습니다: " + id);
	}

	@Transactional
	public void delete(Long id) {
		WorkOrder wo = Guard.requireFound(workOrderRepository.findById(id), "작업지시를 찾을 수 없습니다: " + id);
		Guard.require(wo.canDelete(), "초안 상태의 작업지시만 삭제할 수 있습니다.");
		workOrderRepository.deleteById(id);
	}

	/**
	 * DRAFT → CONFIRMED.
	 * 각 투입 자재에 대해 InventoryService.reserveMaterial() 호출하여 재고를 선점한다.
	 */
	@Transactional
	public void confirm(Long id) {
		WorkOrder wo = Guard.requireFound(workOrderRepository.findByIdWithMaterials(id), "작업지시를 찾을 수 없습니다: " + id);
		wo.confirm(); // DRAFT 상태 검증 포함

		for (WorkOrderMaterial material : wo.getMaterials()) {
			inventoryService.reserveMaterial(
					material.getWarehouseId(),
					material.getMaterialItemId(),
					material.getPlannedQty(),
					wo.getId());
		}
	}

	/**
	 * DRAFT / CONFIRMED → CANCELLED.
	 * CONFIRMED 상태이면 선점된 자재를 InventoryService.unreserveMaterial()로 해제한다.
	 */
	@Transactional
	public void cancel(Long id) {
		WorkOrder wo = Guard.requireFound(workOrderRepository.findByIdWithMaterials(id), "작업지시를 찾을 수 없습니다: " + id);
		boolean wasConfirmed = wo.isConfirmed();
		wo.cancel(); // 상태 검증 포함

		if (wasConfirmed) {
			for (WorkOrderMaterial material : wo.getMaterials()) {
				inventoryService.unreserveMaterial(
						material.getWarehouseId(),
						material.getMaterialItemId(),
						material.getPlannedQty(),
						wo.getId());
			}
		}
	}

	// --- private helpers ---

	private void validateRequest(WorkOrderRequest request) {
		Guard.requireExists(itemService.exists(request.itemId()), "품목을 찾을 수 없습니다: " + request.itemId());
		Guard.requireExists(warehouseService.exists(request.warehouseId()), "창고를 찾을 수 없습니다: " + request.warehouseId());
		if (request.salesOrderId() != null) {
			salesOrderService.findById(request.salesOrderId());
		}
	}

	/**
	 * BOM 라인을 전개하여 WorkOrderMaterial 스냅샷을 생성한다.
	 * plannedQty = BOM 자재 수량 × 작업지시 계획 생산 수량
	 */
	private void expandBomLines(WorkOrder wo, List<BomLineResponse> bomLines,
			BigDecimal workOrderQty, Long warehouseId) {
		if (bomLines == null || bomLines.isEmpty()) return;
		for (BomLineResponse line : bomLines) {
			BigDecimal materialQty = line.quantity().multiply(workOrderQty);
			WorkOrderMaterial material = WorkOrderMaterial.of(
					wo, line.materialItemId(), warehouseId, materialQty, line.sortOrder());
			wo.addMaterial(material);
		}
	}

	/**
	 * BOM에 연결된 라우팅을 전개하여 WorkOrderRouting 스냅샷을 생성한다.
	 * 라우팅이 없거나 비활성이면 스킵한다.
	 */
	private void expandRoutingSteps(WorkOrder wo, Long bomId) {
		for (RoutingStepData step : routingService.findStepsByBomId(bomId)) {
			wo.addRouting(WorkOrderRouting.of(
					wo, step.routingId(), step.processId(),
					step.stepOrder(), step.standardTime(), step.remarks()));
		}
	}
}
