package com.github.gwiman.mini_mes_backend.quote.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteLineRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteResponse;
import com.github.gwiman.mini_mes_backend.quote.domain.Quote;
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
	private final PartnerService partnerService;
	private final EmployeeService employeeService;
	private final ItemService itemService;

	public List<QuoteResponse> findAll(String quoteNumber, Long partnerId, String statusCode,
		LocalDate fromDate, LocalDate toDate) {
		String quoteNumberPattern = buildLikePattern(quoteNumber);
		return quoteQueryRepository.search(
			quoteNumberPattern, partnerId, statusCode, fromDate, toDate
		);
	}

	public QuoteResponse findById(Long id) {
		return quoteQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + id));
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
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + quoteId));
	}

	@Transactional
	public QuoteResponse create(QuoteRequest request) {
		String quoteNumber = generateQuoteNumber();

		if (!partnerService.exists(request.getPartnerId())) {
			throw new IllegalArgumentException("거래처를 찾을 수 없습니다: " + request.getPartnerId());
		}
		if (request.getEmployeeId() != null && !employeeService.exists(request.getEmployeeId())) {
			throw new IllegalArgumentException("담당자를 찾을 수 없습니다: " + request.getEmployeeId());
		}

		Quote quote = new Quote(
			quoteNumber,
			request.getQuoteDate(),
			request.getValidUntil(),
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.getLines()) {
			if (!itemService.exists(lineReq.getItemId())) {
				throw new IllegalArgumentException("품목을 찾을 수 없습니다: " + lineReq.getItemId());
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
		return quoteQueryRepository.findByIdWithLines(saved.getId()).orElseThrow();
	}

	@Transactional
	public QuoteResponse update(Long id, QuoteRequest request) {
		Quote quote = quoteRepository.findByIdWithLines(id)
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + id));

		if (!partnerService.exists(request.getPartnerId())) {
			throw new IllegalArgumentException("거래처를 찾을 수 없습니다: " + request.getPartnerId());
		}
		if (request.getEmployeeId() != null && !employeeService.exists(request.getEmployeeId())) {
			throw new IllegalArgumentException("담당자를 찾을 수 없습니다: " + request.getEmployeeId());
		}

		quote.update(
			request.getQuoteDate(),
			request.getValidUntil(),
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		quote.clearLines();
		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.getLines()) {
			if (!itemService.exists(lineReq.getItemId())) {
				throw new IllegalArgumentException("품목을 찾을 수 없습니다: " + lineReq.getItemId());
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

		return quoteQueryRepository.findByIdWithLines(id).orElseThrow();
	}

	@Transactional
	public void delete(Long id) {
		if (!quoteRepository.existsById(id)) {
			throw new IllegalArgumentException("견적을 찾을 수 없습니다: " + id);
		}
		quoteRepository.deleteById(id);
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

	private String buildLikePattern(String value) {
		if (value == null || value.isBlank()) return null;
		String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + escaped + "%";
	}
}
