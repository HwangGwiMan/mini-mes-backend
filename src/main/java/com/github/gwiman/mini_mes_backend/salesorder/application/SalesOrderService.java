package com.github.gwiman.mini_mes_backend.salesorder.application;

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
import com.github.gwiman.mini_mes_backend.quote.domain.Quote;
import com.github.gwiman.mini_mes_backend.quote.domain.QuoteRepository;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderLineRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderResponse;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrder;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderLine;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderRepository;
import com.github.gwiman.mini_mes_backend.salesorder.infrastructure.SalesOrderQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesOrderService {

	private static final String ORDER_NUMBER_PREFIX = "SO_";
	private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
	private static final Pattern SEQUENCE_PATTERN = Pattern.compile(".*_(\\d+)$");
	private static final String QUOTE_STATUS_ORDERED = "QUOTE_STATUS_05";

	private final SalesOrderRepository salesOrderRepository;
	private final SalesOrderQueryRepository salesOrderQueryRepository;
	private final PartnerRepository partnerRepository;
	private final EmployeeRepository employeeRepository;
	private final ItemRepository itemRepository;
	private final QuoteRepository quoteRepository;

	public List<SalesOrderResponse> findAll(String orderNumber, Long partnerId, String statusCode,
		LocalDate fromDate, LocalDate toDate) {
		String orderNumberPattern = buildLikePattern(orderNumber);
		return salesOrderQueryRepository.search(orderNumberPattern, partnerId, statusCode, fromDate, toDate);
	}

	public SalesOrderResponse findById(Long id) {
		return salesOrderQueryRepository.findByIdWithLines(id)
			.orElseThrow(() -> new IllegalArgumentException("수주를 찾을 수 없습니다: " + id));
	}

	@Transactional
	public SalesOrderResponse create(SalesOrderRequest request) {
		String orderNumber = generateOrderNumber();
		Partner partner = loadPartner(request.getPartnerId());
		Employee employee = loadEmployee(request.getEmployeeId());
		Quote quote = loadQuote(request.getQuoteId());

		SalesOrder order = new SalesOrder(
			orderNumber,
			request.getOrderDate(),
			request.getDeliveryDate(),
			partner,
			employee,
			quote,
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		addLines(order, request.getLines());

		SalesOrder saved = salesOrderRepository.save(order);
		return salesOrderQueryRepository.findByIdWithLines(saved.getId()).orElseThrow();
	}

	@Transactional
	public SalesOrderResponse update(Long id, SalesOrderRequest request) {
		SalesOrder order = salesOrderRepository.findByIdWithLines(id)
			.orElseThrow(() -> new IllegalArgumentException("수주를 찾을 수 없습니다: " + id));

		Partner partner = loadPartner(request.getPartnerId());
		Employee employee = loadEmployee(request.getEmployeeId());

		order.update(
			request.getOrderDate(),
			request.getDeliveryDate(),
			partner,
			employee,
			request.getStatusCode() != null ? request.getStatusCode() : "",
			request.getRemarks() != null ? request.getRemarks() : ""
		);

		order.clearLines();
		addLines(order, request.getLines());

		return salesOrderQueryRepository.findByIdWithLines(id).orElseThrow();
	}

	@Transactional
	public void delete(Long id) {
		if (!salesOrderRepository.existsById(id)) {
			throw new IllegalArgumentException("수주를 찾을 수 없습니다: " + id);
		}
		salesOrderRepository.deleteById(id);
	}

	@Transactional
	public SalesOrderResponse convertFromQuote(Long quoteId) {
		Quote quote = quoteRepository.findByIdWithLines(quoteId)
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + quoteId));

		if (salesOrderRepository.existsByQuoteId(quoteId)) {
			throw new IllegalStateException("이미 수주 전환된 견적입니다: " + quote.getQuoteNumber());
		}

		String orderNumber = generateOrderNumber();

		SalesOrder order = new SalesOrder(
			orderNumber,
			LocalDate.now(),
			null,
			quote.getPartner(),
			quote.getEmployee(),
			quote,
			"ORDER_STATUS_01",
			""
		);

		int sortOrder = 0;
		for (var quoteLine : quote.getLines()) {
			BigDecimal amount = quoteLine.getQuantity().multiply(quoteLine.getUnitPrice());
			SalesOrderLine line = new SalesOrderLine(
				order,
				quoteLine.getItem(),
				quoteLine.getQuantity(),
				quoteLine.getUnitPrice(),
				amount,
				quoteLine.getDeliveryRequestDate(),
				quoteLine.getRemarks() != null ? quoteLine.getRemarks() : "",
				sortOrder++
			);
			order.addLine(line);
		}

		SalesOrder saved = salesOrderRepository.save(order);
		quote.updateStatus(QUOTE_STATUS_ORDERED);

		return salesOrderQueryRepository.findByIdWithLines(saved.getId()).orElseThrow();
	}

	private void addLines(SalesOrder order, List<SalesOrderLineRequest> lineRequests) {
		int sortOrder = 0;
		for (SalesOrderLineRequest lineReq : lineRequests) {
			Item item = itemRepository.findById(lineReq.getItemId())
				.orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다: " + lineReq.getItemId()));
			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			SalesOrderLine line = new SalesOrderLine(
				order,
				item,
				lineReq.getQuantity(),
				lineReq.getUnitPrice(),
				amount,
				lineReq.getDeliveryRequestDate(),
				lineReq.getRemarks() != null ? lineReq.getRemarks() : "",
				sortOrder++
			);
			order.addLine(line);
		}
	}

	private Partner loadPartner(Long partnerId) {
		return partnerRepository.findById(partnerId)
			.orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + partnerId));
	}

	private Employee loadEmployee(Long employeeId) {
		if (employeeId == null) return null;
		return employeeRepository.findById(employeeId)
			.orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다: " + employeeId));
	}

	private Quote loadQuote(Long quoteId) {
		if (quoteId == null) return null;
		return quoteRepository.findById(quoteId)
			.orElseThrow(() -> new IllegalArgumentException("견적을 찾을 수 없습니다: " + quoteId));
	}

	private String generateOrderNumber() {
		String prefix = ORDER_NUMBER_PREFIX + LocalDate.now().format(MONTH_FORMAT) + "_";
		String prefixPattern = prefix + "%";
		int maxSeq = salesOrderQueryRepository.findMaxOrderNumberByPrefix(prefixPattern)
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
