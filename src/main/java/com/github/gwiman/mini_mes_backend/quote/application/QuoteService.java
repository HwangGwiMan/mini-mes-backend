package com.github.gwiman.mini_mes_backend.quote.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.employee.domain.Employee;
import com.github.gwiman.mini_mes_backend.employee.domain.EmployeeRepository;
import com.github.gwiman.mini_mes_backend.item.domain.Item;
import com.github.gwiman.mini_mes_backend.item.domain.ItemRepository;
import com.github.gwiman.mini_mes_backend.partner.domain.Partner;
import com.github.gwiman.mini_mes_backend.partner.domain.PartnerRepository;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteLineRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteResponse;
import com.github.gwiman.mini_mes_backend.quote.domain.Quote;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteLine;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

	private static final String QUOTE_NUMBER_PREFIX = "QT_";
	private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
	private static final Pattern SEQUENCE_PATTERN = Pattern.compile(".*_(\\d+)$");

	private final QuoteRepository quoteRepository;
	private final PartnerRepository partnerRepository;
	private final EmployeeRepository employeeRepository;
	private final ItemRepository itemRepository;

	public List<QuoteResponse> findAll(String quoteNumber, Long partnerId, String statusCode,
		LocalDate fromDate, LocalDate toDate) {
		String quoteNumberPattern = buildLikePattern(quoteNumber);
		return quoteRepository.search(
			quoteNumberPattern, partnerId, statusCode, fromDate, toDate
		).stream()
			.map(QuoteResponse::from)
			.toList();
	}

	public QuoteResponse findById(Long id) {
		Quote entity = quoteRepository.findByIdWithLines(id)
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + id));
		return QuoteResponse.from(entity);
	}

	@Transactional
	public QuoteResponse create(QuoteRequest request) {
		String quoteNumber = generateQuoteNumber();

		Partner partner = partnerRepository.findById(request.getPartnerId())
			.orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + request.getPartnerId()));

		Employee employee = null;
		if (request.getEmployeeId() != null) {
			employee = employeeRepository.findById(request.getEmployeeId())
				.orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다: " + request.getEmployeeId()));
		}

		Quote quote = new Quote(
			quoteNumber,
			request.getQuoteDate(),
			request.getValidUntil(),
			partner,
			employee,
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.getLines()) {
			Item item = itemRepository.findById(lineReq.getItemId())
				.orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다: " + lineReq.getItemId()));

			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			QuoteLine line = new QuoteLine(
				quote,
				item,
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
		return QuoteResponse.from(quoteRepository.findByIdWithLines(saved.getId()).orElseThrow());
	}

	@Transactional
	public QuoteResponse update(Long id, QuoteRequest request) {
		Quote quote = quoteRepository.findByIdWithLines(id)
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + id));

		Partner partner = partnerRepository.findById(request.getPartnerId())
			.orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + request.getPartnerId()));

		Employee employee = null;
		if (request.getEmployeeId() != null) {
			employee = employeeRepository.findById(request.getEmployeeId())
				.orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다: " + request.getEmployeeId()));
		}

		quote.update(
			request.getQuoteDate(),
			request.getValidUntil(),
			partner,
			employee,
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		quote.clearLines();
		int sortOrder = 0;
		for (QuoteLineRequest lineReq : request.getLines()) {
			Item item = itemRepository.findById(lineReq.getItemId())
				.orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다: " + lineReq.getItemId()));

			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			QuoteLine line = new QuoteLine(
				quote,
				item,
				lineReq.getQuantity(),
				lineReq.getUnitPrice(),
				amount,
				lineReq.getDeliveryRequestDate(),
				lineReq.getRemarks(),
				sortOrder++
			);
			quote.addLine(line);
		}

		return QuoteResponse.from(quoteRepository.findByIdWithLines(id).orElseThrow());
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
		int maxSeq = quoteRepository.findMaxQuoteNumberByPrefix(prefixPattern)
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
