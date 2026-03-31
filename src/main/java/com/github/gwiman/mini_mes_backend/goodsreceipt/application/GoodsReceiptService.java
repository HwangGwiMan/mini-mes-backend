package com.github.gwiman.mini_mes_backend.goodsreceipt.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.Guard;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.goodsreceipt.api.GoodsReceiptLineRequest;
import com.github.gwiman.mini_mes_backend.goodsreceipt.api.GoodsReceiptRequest;
import com.github.gwiman.mini_mes_backend.goodsreceipt.api.GoodsReceiptResponse;
import com.github.gwiman.mini_mes_backend.goodsreceipt.domain.GoodsReceipt;
import com.github.gwiman.mini_mes_backend.goodsreceipt.domain.GoodsReceiptLine;
import com.github.gwiman.mini_mes_backend.goodsreceipt.domain.GoodsReceiptRepository;
import com.github.gwiman.mini_mes_backend.goodsreceipt.internal.GoodsReceiptQueryRepository;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
import com.github.gwiman.mini_mes_backend.purchaseorder.application.GoodsReceiptConfirmedEvent;
import com.github.gwiman.mini_mes_backend.purchaseorder.application.PurchaseOrderService;

import lombok.RequiredArgsConstructor;

/**
 * 자재 입고 애플리케이션 서비스.
 * <p>
 * 구매 발주(PO) 연결 또는 직접 입고 두 가지 방식을 지원한다.
 * 입고 확정 시 연결된 PO가 있으면 PO를 입고완료(PO_STATUS_03)로 전이한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsReceiptService {

	private static final String GR_NUMBER_PREFIX = "GR_";

	private final GoodsReceiptRepository goodsReceiptRepository;
	private final GoodsReceiptQueryRepository goodsReceiptQueryRepository;
	private final PurchaseOrderService purchaseOrderService;
	private final PartnerService partnerService;
	private final ItemService itemService;
	private final DocumentNumberGenerator documentNumberGenerator;
	private final ApplicationEventPublisher events;

	public List<GoodsReceiptResponse> findAll(String receiptNumber, String partnerName, String statusCode) {
		String receiptNumberPattern = QueryParamEscaper.containsLike(receiptNumber);
		String partnerNamePattern   = QueryParamEscaper.containsLike(partnerName);
		return goodsReceiptQueryRepository.search(receiptNumberPattern, partnerNamePattern, statusCode);
	}

	public GoodsReceiptResponse findById(Long id) {
		return Guard.requireFound(goodsReceiptQueryRepository.findByIdWithLines(id), "자재 입고를 찾을 수 없습니다: " + id);
	}

	@Transactional
	public GoodsReceiptResponse create(GoodsReceiptRequest request) {
		validatePartner(request.partnerId());
		validateLineItems(request.lines());
		if (request.poId() != null) {
			// PO 존재 여부 확인 — 없으면 ResourceNotFoundException 발생
			purchaseOrderService.findHeaderById(request.poId());
		}

		String receiptNumber = generateReceiptNumber();
		GoodsReceipt gr = GoodsReceipt.create(
			receiptNumber, request.receiptDate(),
			request.poId(), request.partnerId(), request.remarks()
		);
		addLines(gr, request.lines());

		GoodsReceipt saved = goodsReceiptRepository.save(gr);
		return Guard.requireFound(goodsReceiptQueryRepository.findByIdWithLines(saved.getId()), "저장된 자재 입고를 조회할 수 없습니다: " + saved.getId());
	}

	@Transactional
	public GoodsReceiptResponse update(Long id, GoodsReceiptRequest request) {
		GoodsReceipt gr = Guard.requireFound(goodsReceiptRepository.findByIdWithLines(id), "자재 입고를 찾을 수 없습니다: " + id);
		validatePartner(request.partnerId());
		validateLineItems(request.lines());
		if (request.poId() != null) {
			purchaseOrderService.findHeaderById(request.poId());
		}

		gr.update(request.receiptDate(), request.poId(), request.partnerId(), request.remarks());
		gr.clearLines();
		addLines(gr, request.lines());

		return Guard.requireFound(goodsReceiptQueryRepository.findByIdWithLines(id), "저장된 자재 입고를 조회할 수 없습니다: " + id);
	}

	@Transactional
	public void delete(Long id) {
		GoodsReceipt gr = Guard.requireFound(goodsReceiptRepository.findById(id), "자재 입고를 찾을 수 없습니다: " + id);
		Guard.require(gr.canDelete(), "초안 상태의 입고만 삭제할 수 있습니다.");
		goodsReceiptRepository.deleteById(id);
	}

	/**
	 * 초안(GR_STATUS_01) → 입고완료(GR_STATUS_02).
	 * 연결된 PO가 있으면 GoodsReceiptConfirmedEvent를 발행해 PO를 입고완료(PO_STATUS_03)로 전이한다.
	 */
	@Transactional
	public void confirm(Long id) {
		GoodsReceipt gr = Guard.requireFound(goodsReceiptRepository.findById(id), "자재 입고를 찾을 수 없습니다: " + id);
		gr.confirm();
		if (gr.getPoId() != null) {
			events.publishEvent(new GoodsReceiptConfirmedEvent(gr.getId(), gr.getPoId()));
		}
	}

	/** 초안(GR_STATUS_01) → 취소(GR_STATUS_03) */
	@Transactional
	public void cancel(Long id) {
		GoodsReceipt gr = Guard.requireFound(goodsReceiptRepository.findById(id), "자재 입고를 찾을 수 없습니다: " + id);
		gr.cancel();
	}

	private void validatePartner(Long partnerId) {
		Guard.requireExists(partnerService.exists(partnerId), "거래처를 찾을 수 없습니다: " + partnerId);
	}

	private void validateLineItems(List<GoodsReceiptLineRequest> lines) {
		Set<Long> requestedIds = lines.stream()
			.map(GoodsReceiptLineRequest::itemId)
			.collect(Collectors.toSet());
		Set<Long> existingIds = itemService.findExistingIds(requestedIds);
		requestedIds.stream()
			.filter(itemId -> !existingIds.contains(itemId))
			.findFirst()
			.ifPresent(itemId -> {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + itemId);
			});
	}

	private void addLines(GoodsReceipt gr, List<GoodsReceiptLineRequest> lineRequests) {
		int sortOrder = 0;
		for (GoodsReceiptLineRequest req : lineRequests) {
			gr.addLine(GoodsReceiptLine.of(
				gr, req.itemId(), req.poLineId(), req.receiptTypeCode(),
				req.receivedQuantity(), req.unitPrice(), req.remarks(), sortOrder++
			));
		}
	}

	private String generateReceiptNumber() {
		return documentNumberGenerator.generateRaw(GR_NUMBER_PREFIX, "goods_receipt", "receipt_number");
	}
}
