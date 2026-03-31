package com.github.gwiman.mini_mes_backend.quote.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.auth.application.AuthService;
import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.DocumentNumberGenerator;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.quote.api.dto.ApprovalRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.ApprovalResponse;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteLineRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteResponse;
import com.github.gwiman.mini_mes_backend.quote.domain.Quote;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteApproval;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteApprovalRepository;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteLine;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteRepository;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteStatus;
import com.github.gwiman.mini_mes_backend.quote.internal.QuoteQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

	private static final String QUOTE_NUMBER_PREFIX = "QT_";

	private final QuoteRepository quoteRepository;
	private final QuoteQueryRepository quoteQueryRepository;
	private final QuoteApprovalRepository quoteApprovalRepository;
	private final QuoteValidator quoteValidator;
	private final EmployeeService employeeService;
	private final AuthService authService;
	private final DocumentNumberGenerator documentNumberGenerator;

	public List<QuoteResponse> findAll(String quoteNumber, Long partnerId, String statusCode,
		LocalDate fromDate, LocalDate toDate) {
		String quoteNumberPattern = QueryParamEscaper.containsLike(quoteNumber);
		return quoteQueryRepository.search(
			quoteNumberPattern, partnerId, statusCode, fromDate, toDate
		);
	}

	public QuoteResponse findById(Long id) {
		return quoteQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + id));
	}

	/** 타 모듈(salesorder)에서 견적 전환 시 필요한 헤더 정보만 반환 — api DTO 직접 노출 방지 */
	public QuoteHeaderData findHeaderById(Long id) {
		return quoteRepository.findById(id)
			.map(q -> new QuoteHeaderData(q.getId(), q.getStatus(), q.getPartnerId(), q.getEmployeeId()))
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + id));
	}

	public List<QuoteLineData> getLines(Long quoteId) {
		return quoteRepository.findByIdWithLines(quoteId)
			.map(quote -> quote.getLines().stream()
				.map(line -> new QuoteLineData(
					line.getItemId(),
					line.getQuantity(),
					line.getUnitPrice(),
					line.getDeliveryRequestDate(),
					line.getRemarks(),
					line.getSortOrder()
				))
				.toList())
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));
	}

	@Transactional
	public QuoteResponse create(QuoteRequest request) {
		quoteValidator.validateHeader(request);
		quoteValidator.validateLines(request.lines());

		String quoteNumber = generateQuoteNumber();

		Quote quote = Quote.create(quoteNumber,
			request.quoteDate(), request.validUntil(),
			request.partnerId(), request.employeeId(),
			request.approverId(), request.remarks());

		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.lines()) {
			quote.addLine(QuoteLine.of(quote,
				lineReq.itemId(), lineReq.quantity(), lineReq.unitPrice(),
				lineReq.deliveryRequestDate(), lineReq.remarks(), sortOrder++));
		}

		Quote saved = quoteRepository.save(quote);
		return quoteQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 견적을 조회할 수 없습니다: " + saved.getId()));
	}

	@Transactional
	public QuoteResponse update(Long id, QuoteRequest request) {
		Quote quote = quoteRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + id));

		quoteValidator.validateHeader(request);

		// Quote.update() will throw if status is QUOTE_STATUS_02
		quote.update(
			request.quoteDate(),
			request.validUntil(),
			request.partnerId(),
			request.employeeId(),
			request.approverId(),
			request.remarks() != null ? request.remarks() : ""
		);

		quoteValidator.validateLines(request.lines());

		quote.clearLines();
		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.lines()) {
			quote.addLine(QuoteLine.of(quote,
				lineReq.itemId(), lineReq.quantity(), lineReq.unitPrice(),
				lineReq.deliveryRequestDate(), lineReq.remarks(), sortOrder++));
		}

		return quoteQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("저장된 견적을 조회할 수 없습니다: " + id));
	}

	@Transactional
	public void delete(Long id) {
		if (!quoteRepository.existsById(id)) {
			throw new ResourceNotFoundException("견적을 찾을 수 없습니다: " + id);
		}
		quoteRepository.deleteById(id);
	}

	@Transactional
	public void submit(Long quoteId, String currentUsername, boolean isAdmin) {
		Quote quote = quoteRepository.findById(quoteId)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));

		if (!quote.canSubmit()) {
			throw new BusinessRuleViolationException("작성중 또는 반려 상태의 견적만 제출할 수 있습니다.");
		}

		String createdBy = quote.getCreatedBy();
		if (!isAdmin && createdBy != null && !createdBy.equals(currentUsername)) {
			throw new BusinessRuleViolationException("견적 등록자 또는 관리자만 제출할 수 있습니다.");
		}

		quote.updateStatus(QuoteStatus.SUBMITTED);
	}

	@Transactional
	public void approve(Long quoteId, String currentUsername, ApprovalRequest request) {
		Quote quote = quoteRepository.findById(quoteId)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));

		if (!quote.canApprove()) {
			throw new BusinessRuleViolationException("제출 상태의 견적만 승인할 수 있습니다.");
		}

		Long currentEmployeeId = authService.findEmployeeIdByUsername(currentUsername);
		if (currentEmployeeId == null || !currentEmployeeId.equals(quote.getApproverId())) {
			throw new BusinessRuleViolationException("지정된 결재자만 승인할 수 있습니다.");
		}

		String approverName = employeeService.findNameById(currentEmployeeId);
		quoteApprovalRepository.save(new QuoteApproval(
			quoteId, currentEmployeeId, currentUsername,
			approverName, "APPROVED", request.comment()
		));

		quote.updateStatus(QuoteStatus.APPROVED);
	}

	@Transactional
	public void reject(Long quoteId, String currentUsername, ApprovalRequest request) {
		Quote quote = quoteRepository.findById(quoteId)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));

		if (!quote.canApprove()) {
			throw new BusinessRuleViolationException("제출 상태의 견적만 반려할 수 있습니다.");
		}

		Long currentEmployeeId = authService.findEmployeeIdByUsername(currentUsername);
		if (currentEmployeeId == null || !currentEmployeeId.equals(quote.getApproverId())) {
			throw new BusinessRuleViolationException("지정된 결재자만 반려할 수 있습니다.");
		}

		String approverName = employeeService.findNameById(currentEmployeeId);
		quoteApprovalRepository.save(new QuoteApproval(
			quoteId, currentEmployeeId, currentUsername,
			approverName, "REJECTED", request.comment()
		));

		quote.updateStatus(QuoteStatus.REJECTED);
	}

	public List<ApprovalResponse> getApprovalHistory(Long quoteId) {
		if (!quoteRepository.existsById(quoteId)) {
			throw new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId);
		}
		return quoteApprovalRepository.findByQuoteIdOrderByCreatedAtAsc(quoteId).stream()
			.map(ApprovalResponse::from)
			.toList();
	}

	private String generateQuoteNumber() {
		return documentNumberGenerator.generate(
			QUOTE_NUMBER_PREFIX,
			com.github.gwiman.mini_mes_backend.jooq.tables.Quote.QUOTE.QUOTE_NUMBER
		);
	}

}
