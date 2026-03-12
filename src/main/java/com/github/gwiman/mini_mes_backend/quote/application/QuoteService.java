package com.github.gwiman.mini_mes_backend.quote.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.auth.application.AuthService;
import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeResponse;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
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
import com.github.gwiman.mini_mes_backend.quote.internal.QuoteQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

	private static final String QUOTE_NUMBER_PREFIX = "QT_";
	private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
	private static final Pattern SEQUENCE_PATTERN = Pattern.compile(".*_(\\d+)$");

	private final QuoteRepository quoteRepository;
	private final QuoteQueryRepository quoteQueryRepository;
	private final QuoteApprovalRepository quoteApprovalRepository;
	private final PartnerService partnerService;
	private final EmployeeService employeeService;
	private final ItemService itemService;
	private final AuthService authService;

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
		String quoteNumber = generateQuoteNumber();
		String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

		if (!partnerService.exists(request.getPartnerId())) {
			throw new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + request.getPartnerId());
		}
		if (request.getEmployeeId() != null && !employeeService.exists(request.getEmployeeId())) {
			throw new ResourceNotFoundException("담당자를 찾을 수 없습니다: " + request.getEmployeeId());
		}
		if (!employeeService.exists(request.getApproverId())) {
			throw new ResourceNotFoundException("결재자를 찾을 수 없습니다: " + request.getApproverId());
		}

		Quote quote = new Quote(
			quoteNumber,
			request.getQuoteDate(),
			request.getValidUntil(),
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getApproverId(),
			"QUOTE_STATUS_01",
			request.getRemarks() != null ? request.getRemarks() : "",
			currentUsername
		);

		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.getLines()) {
			if (!itemService.exists(lineReq.getItemId())) {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + lineReq.getItemId());
			}

			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			QuoteLine line = new QuoteLine(
				quote,
				lineReq.getItemId(),
				lineReq.getQuantity(),
				lineReq.getUnitPrice(),
				amount,
				lineReq.getDeliveryRequestDate(),
				lineReq.getRemarks(),
				sortOrder++
			);
			quote.addLine(line);
		}

		Quote saved = quoteRepository.save(quote);
		return quoteQueryRepository.findByIdWithLines(saved.getId())
			.orElseThrow(() -> new ResourceNotFoundException("저장된 견적을 조회할 수 없습니다: " + saved.getId()));
	}

	@Transactional
	public QuoteResponse update(Long id, QuoteRequest request) {
		Quote quote = quoteRepository.findByIdWithLines(id)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + id));

		if (!partnerService.exists(request.getPartnerId())) {
			throw new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + request.getPartnerId());
		}
		if (request.getEmployeeId() != null && !employeeService.exists(request.getEmployeeId())) {
			throw new ResourceNotFoundException("담당자를 찾을 수 없습니다: " + request.getEmployeeId());
		}
		if (!employeeService.exists(request.getApproverId())) {
			throw new ResourceNotFoundException("결재자를 찾을 수 없습니다: " + request.getApproverId());
		}

		// Quote.update() will throw if status is QUOTE_STATUS_02
		quote.update(
			request.getQuoteDate(),
			request.getValidUntil(),
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getApproverId(),
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		quote.clearLines();
		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.getLines()) {
			if (!itemService.exists(lineReq.getItemId())) {
				throw new ResourceNotFoundException("품목을 찾을 수 없습니다: " + lineReq.getItemId());
			}

			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			QuoteLine line = new QuoteLine(
				quote,
				lineReq.getItemId(),
				lineReq.getQuantity(),
				lineReq.getUnitPrice(),
				amount,
				lineReq.getDeliveryRequestDate(),
				lineReq.getRemarks(),
				sortOrder++
			);
			quote.addLine(line);
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
	public void submit(Long quoteId, String currentUsername) {
		Quote quote = quoteRepository.findById(quoteId)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));

		String status = quote.getStatusCode();
		if (!"QUOTE_STATUS_01".equals(status) && !"QUOTE_STATUS_04".equals(status)) {
			throw new BusinessRuleViolationException("작성중 또는 반려 상태의 견적만 제출할 수 있습니다.");
		}

		boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
			.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		String createdBy = quote.getCreatedBy();
		if (!isAdmin && createdBy != null && !createdBy.equals(currentUsername)) {
			throw new BusinessRuleViolationException("견적 등록자 또는 관리자만 제출할 수 있습니다.");
		}

		quote.updateStatus("QUOTE_STATUS_02");
	}

	@Transactional
	public void approve(Long quoteId, String currentUsername, ApprovalRequest request) {
		Quote quote = quoteRepository.findById(quoteId)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));

		if (!"QUOTE_STATUS_02".equals(quote.getStatusCode())) {
			throw new BusinessRuleViolationException("제출 상태의 견적만 승인할 수 있습니다.");
		}

		Long currentEmployeeId = authService.findEmployeeIdByUsername(currentUsername);
		if (currentEmployeeId == null || !currentEmployeeId.equals(quote.getApproverId())) {
			throw new BusinessRuleViolationException("지정된 결재자만 승인할 수 있습니다.");
		}

		EmployeeResponse approver = employeeService.findById(currentEmployeeId);
		quoteApprovalRepository.save(new QuoteApproval(
			quoteId, currentEmployeeId, currentUsername,
			approver.getName(), "APPROVED", request.getComment()
		));

		quote.updateStatus("QUOTE_STATUS_03");
	}

	@Transactional
	public void reject(Long quoteId, String currentUsername, ApprovalRequest request) {
		Quote quote = quoteRepository.findById(quoteId)
			.orElseThrow(() -> new ResourceNotFoundException("견적을 찾을 수 없습니다: " + quoteId));

		if (!"QUOTE_STATUS_02".equals(quote.getStatusCode())) {
			throw new BusinessRuleViolationException("제출 상태의 견적만 반려할 수 있습니다.");
		}

		Long currentEmployeeId = authService.findEmployeeIdByUsername(currentUsername);
		if (currentEmployeeId == null || !currentEmployeeId.equals(quote.getApproverId())) {
			throw new BusinessRuleViolationException("지정된 결재자만 반려할 수 있습니다.");
		}

		EmployeeResponse approver = employeeService.findById(currentEmployeeId);
		quoteApprovalRepository.save(new QuoteApproval(
			quoteId, currentEmployeeId, currentUsername,
			approver.getName(), "REJECTED", request.getComment()
		));

		quote.updateStatus("QUOTE_STATUS_04");
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
		String prefix = QUOTE_NUMBER_PREFIX + LocalDate.now().format(MONTH_FORMAT) + "_";
		String prefixPattern = prefix + "%";
		int maxSeq = quoteQueryRepository.findMaxQuoteNumberByPrefix(prefixPattern)
			.stream()
			.mapToInt(s -> {
				var m = SEQUENCE_PATTERN.matcher(s);
				return m.find() ? Integer.parseInt(m.group(1)) : 0;
			})
			.max()
			.orElse(0);
		return prefix + String.format("%03d", maxSeq + 1);
	}

}
