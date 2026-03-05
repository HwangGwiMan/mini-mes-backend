package com.github.gwiman.mini_mes_backend.salesorder.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderRequest;
import com.github.gwiman.mini_mes_backend.salesorder.api.dto.SalesOrderResponse;
import com.github.gwiman.mini_mes_backend.salesorder.application.SalesOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

	private final SalesOrderService salesOrderService;

	@GetMapping
	public List<SalesOrderResponse> getAll(
		@RequestParam(required = false) String orderNumber,
		@RequestParam(required = false) Long partnerId,
		@RequestParam(required = false) String statusCode,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return salesOrderService.findAll(orderNumber, partnerId, statusCode, fromDate, toDate);
	}

	@GetMapping("/{id}")
	public SalesOrderResponse getById(@PathVariable Long id) {
		return salesOrderService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SalesOrderResponse create(@RequestBody @Valid SalesOrderRequest request) {
		return salesOrderService.create(request);
	}

	@PutMapping("/{id}")
	public SalesOrderResponse update(@PathVariable Long id, @RequestBody @Valid SalesOrderRequest request) {
		return salesOrderService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		salesOrderService.delete(id);
	}

	@PostMapping("/from-quote/{quoteId}")
	@ResponseStatus(HttpStatus.CREATED)
	public SalesOrderResponse convertFromQuote(@PathVariable Long quoteId) {
		return salesOrderService.convertFromQuote(quoteId);
	}
}
