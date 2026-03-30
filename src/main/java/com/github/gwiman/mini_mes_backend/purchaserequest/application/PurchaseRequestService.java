package com.github.gwiman.mini_mes_backend.purchaserequest.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestLineRequest;
import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestRequest;
import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestResponse;
import com.github.gwiman.mini_mes_backend.purchaserequest.domain.PurchaseRequest;
import com.github.gwiman.mini_mes_backend.purchaserequest.domain.PurchaseRequestLine;
import com.github.gwiman.mini_mes_backend.purchaserequest.domain.PurchaseRequestRepository;
import com.github.gwiman.mini_mes_backend.purchaserequest.internal.PurchaseRequestQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 구매 요청 애플리케이션 서비스.
 * CRUD 및 승인 워크플로(제출 → 승인/반려 → 발주 전환)를 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseRequestService {

	private static final String PR_NUMBER_PREFIX = "PR_";

	private final PurchaseRequestRepository purchaseRequestRepository;
	private final PurchaseRequestQueryRepository purchaseRequestQueryRepository;
	private final EmployeeService employeeService;
	private final ItemService itemService;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<PurchaseRequestResponse> findAll(String requestNumber, Long requesterId,
			String statusCode, LocalDate fromDate, LocalDate toDate) {
		String requestNumberPattern = QueryParamEscaper.containsLike(requestNumber);
		return purchaseRequestQueryRepository.search(
			requestNumberPattern, requesterId, statusCode, fromDate, toDate
		);
	}

	public PurchaseRequestResponse findById(Long id) {
		return purchaseRequestQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
	}

	/** 타 모듈(purchaseorder)에서 전환 시 필요한 헤더 정보만 반환 — api DTO 직접 노출 방지 */
	public PurchaseRequestHeaderData findHeaderById(Long id) {
		return purchaseRequestRepository.findById(id)
			.map(pr -> new PurchaseRequestHeaderData(
				pr.getId(), pr.getRequestNumber(), pr.getStatusCode(), pr.getRequesterId()))
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
	}

	/** 타 모듈(purchaseorder)에서 라인 복사 시 사용 */
	public List<PurchaseRequestLineData> getLines(Long purchaseRequestId) {
		return purchaseRequestRepository.findByIdWithLines(purchaseRequestId)
			.map(pr -> pr.getLines().stream()
				.map(line -> new PurchaseRequestLineData(
					line.getId(),
					line.getItemId(),
					line.getRequestedQuantity(),
					line.getRequiredDate(),
					line.getRemarks(),
					line.getSortOrder()
				))
				.toList())
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + purchaseRequestId));
	}

	@Transactional
	public PurchaseRequestResponse create(PurchaseRequestRequest request) {
		validateHeader(request);
		validateLines(request.lines());

		String requestNumber = generateRequestNumber();
		PurchaseRequest pr = PurchaseRequest.create(
			requestNumber, request.requestDate(), request.requesterId(), request.remarks()
		);

		int sortOrder = 0;
		for (PurchaseRequestLineRequest lineReq : request.lines()) {
			pr.addLine(PurchaseRequestLine.of(
				pr, lineReq.itemId(), lineReq.requestedQuantity(),
				lineReq.requiredDate(), lineReq.remarks(), sortOrder++
			));
		}

		PurchaseRequest saved = purchaseRequestRepository.save(pr);
		return purchaseRequestQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 구매 요청을 조회할 수 없습니다: " + saved.getId()));
	}

	@Transactional
	public PurchaseRequestResponse update(Long id, PurchaseRequestRequest request) {
		PurchaseRequest pr = purchaseRequestRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));

		// PurchaseRequest.update() 내부에서 canEdit() 검증
		pr.update(request.requestDate(), request.requesterId(),
			request.remarks() != null ? request.remarks() : "");

		validateLines(request.lines());

		pr.clearLines();
		int sortOrder = 0;
		for (PurchaseRequestLineRequest lineReq : request.lines()) {
			pr.addLine(PurchaseRequestLine.of(
				pr, lineReq.itemId(), lineReq.requestedQuantity(),
				lineReq.requiredDate(), lineReq.remarks(), sortOrder++
			));
		}

		return purchaseRequestQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 구매 요청을 조회할 수 없습니다: " + id));
	}

	@Transactional
	public void delete(Long id) {
		PurchaseRequest pr = purchaseRequestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
		// 초안(01) 상태만 삭제 가능 — 제출 이후는 이력이 남으므로 삭제 불가
		if (!"PR_STATUS_01".equals(pr.getStatusCode())) {
			throw new BusinessRuleViolationException("초안 상태의 구매 요청만 삭제할 수 있습니다.");
		}
		purchaseRequestRepository.deleteById(id);
	}

	/** 초안(01)/반려됨(04) → 검토중(02) */
	@Transactional
	public void submit(Long id) {
		PurchaseRequest pr = purchaseRequestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
		pr.submit();
	}

	/** 검토중(02) → 승인됨(03) */
	@Transactional
	public void approve(Long id) {
		PurchaseRequest pr = purchaseRequestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
		pr.approve();
	}

	/** 검토중(02) → 반려됨(04) */
	@Transactional
	public void reject(Long id) {
		PurchaseRequest pr = purchaseRequestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
		pr.reject();
	}

	/**
	 * 발주됨(05) → 승인됨(03) 복원.
	 * PurchaseOrderService에서 PO 취소 시 pr_id가 있으면 호출한다.
	 */
	@Transactional
	public void markUnordered(Long id) {
		PurchaseRequest pr = purchaseRequestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
		pr.markUnordered();
	}

	/**
	 * 승인됨(03) → 발주됨(05).
	 * PurchaseOrderService에서 발주 전환 시 내부적으로 호출한다.
	 */
	@Transactional
	public void markOrdered(Long id) {
		PurchaseRequest pr = purchaseRequestRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("구매 요청을 찾을 수 없습니다: " + id));
		pr.markOrdered();
	}

	private void validateHeader(PurchaseRequestRequest request) {
		if (request.requesterId() != null && !employeeService.exists(request.requesterId())) {
			throw new ResourceNotFoundException("요청자를 찾을 수 없습니다: " + request.requesterId());
		}
	}

	/** 라인 품목 존재 여부를 IN 쿼리 한 번으로 일괄 검증 — N+1 방지 */
	private void validateLines(List<PurchaseRequestLineRequest> lines) {
		Set<Long> requestedIds = lines.stream()
			.map(PurchaseRequestLineRequest::itemId)
			.collect(Collectors.toSet());
		Set<Long> existingIds = itemService.findExistingIds(requestedIds);
		requestedIds.stream()
			.filter(id -> !existingIds.contains(id))
			.findFirst()
			.ifPresent(id -> {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + id);
			});
	}

	/** purchase_request 테이블은 jOOQ 코드 생성 전이므로 generateRaw 사용 */
	private String generateRequestNumber() {
		return documentNumberGenerator.generateRaw(PR_NUMBER_PREFIX, "purchase_request", "request_number");
	}
}
