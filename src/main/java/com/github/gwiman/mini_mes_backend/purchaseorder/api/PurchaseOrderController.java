package com.github.gwiman.mini_mes_backend.purchaseorder.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderRequest;
import com.github.gwiman.mini_mes_backend.purchaseorder.api.dto.PurchaseOrderResponse;
import com.github.gwiman.mini_mes_backend.purchaseorder.application.PurchaseOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

	private final PurchaseOrderService purchaseOrderService;

	@GetMapping
	public List<PurchaseOrderResponse> getAll(
			@RequestParam(required = false) String orderNumber,
			@RequestParam(required = false) String partnerName,
			@RequestParam(required = false) String statusCode) {
		return purchaseOrderService.findAll(orderNumber, partnerName, statusCode);
	}

	@GetMapping("/{id}")
	public PurchaseOrderResponse getById(@PathVariable Long id) {
		return purchaseOrderService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseOrderResponse create(@RequestBody @Valid PurchaseOrderRequest request) {
		return purchaseOrderService.create(request);
	}

	@PostMapping("/from-pr/{prId}")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseOrderResponse createFromPr(
			@PathVariable Long prId,
			@RequestBody @Valid PurchaseOrderRequest request) {
		return purchaseOrderService.createFromPr(prId, request);
	}

	@PutMapping("/{id}")
	public PurchaseOrderResponse update(
			@PathVariable Long id,
			@RequestBody @Valid PurchaseOrderRequest request) {
		return purchaseOrderService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		purchaseOrderService.delete(id);
	}

	@PatchMapping("/{id}/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirm(@PathVariable Long id) {
		purchaseOrderService.confirm(id);
	}

	@PatchMapping("/{id}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable Long id) {
		purchaseOrderService.cancel(id);
	}
}
