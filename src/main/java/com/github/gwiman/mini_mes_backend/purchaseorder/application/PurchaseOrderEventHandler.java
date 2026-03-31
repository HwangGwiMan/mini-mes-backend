package com.github.gwiman.mini_mes_backend.purchaseorder.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.purchaseorder.domain.PurchaseOrderRepository;

import lombok.RequiredArgsConstructor;

/**
 * 구매 발주 관련 도메인 이벤트 핸들러.
 * goodsreceipt 모듈이 발행하는 이벤트를 수신해 PO 상태를 전이한다.
 */
@Component
@RequiredArgsConstructor
public class PurchaseOrderEventHandler {

	private final PurchaseOrderRepository purchaseOrderRepository;

	// @ApplicationModuleListener는 내부적으로 @TransactionalEventListener를 포함하므로 @Transactional 중복 불가
	@ApplicationModuleListener
	public void on(GoodsReceiptConfirmedEvent event) {
		purchaseOrderRepository.findById(event.poId())
			.ifPresent(po -> po.markReceived());
	}
}
