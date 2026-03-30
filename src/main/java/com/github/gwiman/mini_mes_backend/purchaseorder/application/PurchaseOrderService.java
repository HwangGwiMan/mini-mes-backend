package com.github.gwiman.mini_mes_backend.purchaseorder.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderLineRequest;
import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderRequest;
import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderResponse;
import com.github.gwiman.mini_mes_backend.purchaseorder.domain.PurchaseOrder;
import com.github.gwiman.mini_mes_backend.purchaseorder.domain.PurchaseOrderLine;
import com.github.gwiman.mini_mes_backend.purchaseorder.domain.PurchaseOrderRepository;
import com.github.gwiman.mini_mes_backend.purchaseorder.internal.PurchaseOrderQueryRepository;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseRequestService;

import lombok.RequiredArgsConstructor;

/**
 * 구매 발주 애플리케이션 서비스.
 * <p>
 * 직접 생성과 구매 요청(PR) 전환 두 가지 방법으로 발주를 생성한다.
 * PR 전환 시 PR 상태를 발주됨(05)으로 변경하고, PO 취소 시 PR을 승인됨(03)으로 복원한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

	private static final String PO_NUMBER_PREFIX = "PO_";

	private final PurchaseOrderRepository purchaseOrderRepository;
	private final PurchaseOrderQueryRepository purchaseOrderQueryRepository;
	private final PurchaseRequestService purchaseRequestService;
	private final PartnerService partnerService;
	private final ItemService itemService;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<PurchaseOrderResponse> findAll(String orderNumber, String partnerName, String statusCode) {
		String orderNumberPattern = QueryParamEscaper.containsLike(orderNumber);
		String partnerNamePattern = QueryParamEscaper.containsLike(partnerName);
		return purchaseOrderQueryRepository.search(orderNumberPattern, partnerNamePattern, statusCode);
	}

	public PurchaseOrderResponse findById(Long id) {
		return purchaseOrderQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
	}

	/** Phase 3 자재입고(PurchaseReceipt) 도메인에서 참조용 */
	public PurchaseOrderHeaderData findHeaderById(Long id) {
		return purchaseOrderRepository.findById(id)
			.map(po -> new PurchaseOrderHeaderData(
				po.getId(), po.getOrderNumber(), po.getStatusCode(), po.getPartnerId()))
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
	}

	/** Phase 3 자재입고에서 발주 라인 조회용 */
	public PurchaseOrderResponse findByIdWithLines(Long id) {
		return findById(id);
	}

	@Transactional
	public PurchaseOrderResponse create(PurchaseOrderRequest request) {
		validatePartner(request.partnerId());
		validateLineItems(request.lines());

		String orderNumber = generateOrderNumber();
		PurchaseOrder po = PurchaseOrder.create(
			orderNumber, request.orderDate(), request.partnerId(),
			request.expectedArrivalDate(), request.remarks()
		);
		addLines(po, request.lines());

		PurchaseOrder saved = purchaseOrderRepository.save(po);
		return purchaseOrderQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 구매 발주를 조회할 수 없습니다: " + saved.getId()));
	}

	/**
	 * 구매 요청(PR) 전환으로 발주 생성.
	 * PR 상태가 승인됨(03)이어야 하며, 전환 후 PR 상태를 발주됨(05)으로 변경한다.
	 * 요청 DTO의 lines에는 prLineId가 포함되어야 하며 품목/수량은 PR 기준으로 pre-fill된다.
	 */
	@Transactional
	public PurchaseOrderResponse createFromPr(Long prId, PurchaseOrderRequest request) {
		var prHeader = purchaseRequestService.findHeaderById(prId);
		if (!"PR_STATUS_03".equals(prHeader.statusCode())) {
			throw new BusinessRuleViolationException("승인된 구매 요청만 발주 전환할 수 있습니다.");
		}
		validatePartner(request.partnerId());
		validateLineItems(request.lines());

		String orderNumber = generateOrderNumber();
		PurchaseOrder po = PurchaseOrder.fromPurchaseRequest(
			orderNumber, request.orderDate(), request.partnerId(),
			request.expectedArrivalDate(), prId, request.remarks()
		);
		addLines(po, request.lines());

		purchaseRequestService.markOrdered(prId);

		PurchaseOrder saved = purchaseOrderRepository.save(po);
		return purchaseOrderQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 구매 발주를 조회할 수 없습니다: " + saved.getId()));
	}

	@Transactional
	public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
		PurchaseOrder po = purchaseOrderRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
		validatePartner(request.partnerId());
		validateLineItems(request.lines());

		po.update(request.orderDate(), request.partnerId(),
				request.expectedArrivalDate(), request.remarks());
		po.clearLines();
		addLines(po, request.lines());

		return purchaseOrderQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 구매 발주를 조회할 수 없습니다: " + id));
	}

	@Transactional
	public void delete(Long id) {
		PurchaseOrder po = purchaseOrderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
		if (!po.canDelete()) {
			throw new BusinessRuleViolationException("초안 상태의 발주만 삭제할 수 있습니다.");
		}
		purchaseOrderRepository.deleteById(id);
	}

	/** 초안(01) → 발주됨(02) */
	@Transactional
	public void confirm(Long id) {
		PurchaseOrder po = purchaseOrderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
		po.confirm();
	}

	/**
	 * 취소(04).
	 * 연결된 PR이 있으면 PR 상태를 승인됨(03)으로 복원한다.
	 */
	@Transactional
	public void cancel(Long id) {
		PurchaseOrder po = purchaseOrderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
		po.cancel();
		if (po.getPrId() != null) {
			purchaseRequestService.markUnordered(po.getPrId());
		}
	}

	/**
	 * 발주됨(02) → 입고완료(03).
	 * Phase 3 자재입고(PurchaseReceipt) 도메인 구현 후 PurchaseReceiptService에서 호출한다.
	 */
	@Transactional
	public void markReceived(Long id) {
		PurchaseOrder po = purchaseOrderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 발주를 찾을 수 없습니다: " + id));
		po.markReceived();
	}

	private void validatePartner(Long partnerId) {
		if (!partnerService.exists(partnerId)) {
			throw new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + partnerId);
		}
	}

	private void validateLineItems(List<PurchaseOrderLineRequest> lines) {
		Set<Long> requestedIds = lines.stream()
			.map(PurchaseOrderLineRequest::itemId)
			.collect(Collectors.toSet());
		Set<Long> existingIds = itemService.findExistingIds(requestedIds);
		requestedIds.stream()
			.filter(itemId -> !existingIds.contains(itemId))
			.findFirst()
			.ifPresent(itemId -> {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + itemId);
			});
	}

	private void addLines(PurchaseOrder po, List<PurchaseOrderLineRequest> lineRequests) {
		int sortOrder = 0;
		for (PurchaseOrderLineRequest req : lineRequests) {
			po.addLine(PurchaseOrderLine.of(
				po, req.itemId(), req.orderedQuantity(),
				req.unitPrice(), req.requiredDate(),
				req.remarks(), sortOrder++, req.prLineId()
			));
		}
	}

	private String generateOrderNumber() {
		return documentNumberGenerator.generateRaw(PO_NUMBER_PREFIX, "purchase_order", "order_number");
	}

}
