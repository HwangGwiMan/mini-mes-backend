package com.github.gwiman.mini_mes_backend.salesorder.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;
import com.github.gwiman.mini_mes_backend.item.application.ItemService;
import com.github.gwiman.mini_mes_backend.partner.application.PartnerService;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteResponse;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteLineData;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteService;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderLineRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderResponse;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrder;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderLine;
import com.github.gwiman.mini_mes_backend.salesorder.domain.SalesOrderRepository;
import com.github.gwiman.mini_mes_backend.salesorder.internal.SalesOrderQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesOrderService {

	private static final String ORDER_NUMBER_PREFIX = "SO_";
	private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
	private static final Pattern SEQUENCE_PATTERN = Pattern.compile(".*_(\\d+)$");

	private final SalesOrderRepository salesOrderRepository;
	private final SalesOrderQueryRepository salesOrderQueryRepository;
	private final PartnerService partnerService;
	private final EmployeeService employeeService;
	private final ItemService itemService;
	private final QuoteService quoteService;
	private final ApplicationEventPublisher eventPublisher;

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
		validatePartner(request.getPartnerId());
		validateEmployee(request.getEmployeeId());

		SalesOrder order = new SalesOrder(
			orderNumber,
			request.getOrderDate(),
			request.getDeliveryDate(),
			request.getPartnerId(),
			request.getEmployeeId(),
			request.getQuoteId(),
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

		validatePartner(request.getPartnerId());
		validateEmployee(request.getEmployeeId());

		order.update(
			request.getOrderDate(),
			request.getDeliveryDate(),
			request.getPartnerId(),
			request.getEmployeeId(),
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
		if (salesOrderRepository.existsByQuoteId(quoteId)) {
			throw new IllegalStateException("이미 수주 전환된 견적입니다: " + quoteId);
		}

		QuoteResponse quoteHeader = quoteService.findById(quoteId);
		if (!"QUOTE_STATUS_03".equals(quoteHeader.getStatusCode())) {
			throw new IllegalStateException("승인된 견적만 수주전환이 가능합니다.");
		}
		List<QuoteLineData> quoteLines = quoteService.getLines(quoteId);

		String orderNumber = generateOrderNumber();

		SalesOrder order = new SalesOrder(
			orderNumber,
			LocalDate.now(),
			null,
			quoteHeader.getPartnerId(),
			quoteHeader.getEmployeeId(),
			quoteId,
			"ORDER_STATUS_01",
			""
		);

		int sortOrder = 0;
		for (QuoteLineData quoteLine : quoteLines) {
			BigDecimal amount = quoteLine.quantity().multiply(quoteLine.unitPrice());
			SalesOrderLine line = new SalesOrderLine(
				order,
				quoteLine.itemId(),
				quoteLine.quantity(),
				quoteLine.unitPrice(),
				amount,
				quoteLine.deliveryRequestDate(),
				quoteLine.remarks() != null ? quoteLine.remarks() : "",
				sortOrder++
			);
			order.addLine(line);
		}

		SalesOrder saved = salesOrderRepository.save(order);
		eventPublisher.publishEvent(new QuoteConvertedToOrderEvent(quoteId));

		return salesOrderQueryRepository.findByIdWithLines(saved.getId()).orElseThrow();
	}

	private void addLines(SalesOrder order, List<SalesOrderLineRequest> lineRequests) {
		int sortOrder = 0;
		for (SalesOrderLineRequest lineReq : lineRequests) {
			if (!itemService.exists(lineReq.getItemId())) {
				throw new IllegalArgumentException("품목을 찾을 수 없습니다: " + lineReq.getItemId());
			}
			BigDecimal amount = lineReq.getQuantity().multiply(lineReq.getUnitPrice());
			SalesOrderLine line = new SalesOrderLine(
				order,
				lineReq.getItemId(),
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

	private void validatePartner(Long partnerId) {
		if (!partnerService.exists(partnerId)) {
			throw new IllegalArgumentException("거래처를 찾을 수 없습니다: " + partnerId);
		}
	}

	private void validateEmployee(Long employeeId) {
		if (employeeId != null && !employeeService.exists(employeeId)) {
			throw new IllegalArgumentException("담당자를 찾을 수 없습니다: " + employeeId);
		}
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
