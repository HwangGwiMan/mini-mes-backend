package com.github.gwiman.mini_mes_backend.purchaserequest.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.purchaserequest.domain.PurchaseRequestRepository;

import lombok.RequiredArgsConstructor;

/**
 * 구매 요청 관련 도메인 이벤트 핸들러.
 * purchaseorder 모듈이 발행하는 이벤트를 수신해 PR 상태를 전이한다.
 */
@Component
@RequiredArgsConstructor
public class PurchaseRequestEventHandler {

	private final PurchaseRequestRepository purchaseRequestRepository;

	// @ApplicationModuleListener는 내부적으로 @TransactionalEventListener를 포함하므로 @Transactional 중복 불가
	@ApplicationModuleListener
	public void on(PurchaseOrderCreatedFromPREvent event) {
		purchaseRequestRepository.findById(event.prId())
			.ifPresent(pr -> pr.markOrdered());
	}

	@ApplicationModuleListener
	public void on(PurchaseOrderCancelledEvent event) {
		// prId가 null이면 직접 생성 발주이므로 PR 상태 복원 불필요
		if (event.prId() == null) return;
		purchaseRequestRepository.findById(event.prId())
			.ifPresent(pr -> pr.markUnordered());
	}
}
