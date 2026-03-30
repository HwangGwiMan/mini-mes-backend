package com.github.gwiman.mini_mes_backend.purchaserequest.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestRequest;
import com.github.gwiman.mini_mes_backend.purchaserequest.api.dto.PurchaseRequestResponse;
import com.github.gwiman.mini_mes_backend.purchaserequest.application.PurchaseRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 구매 요청 REST 컨트롤러.
 * CRUD 및 승인 워크플로(제출/승인/반려/발주전환) 엔드포인트를 제공한다.
 */
@RestController
@RequestMapping("/api/purchase-requests")
@RequiredArgsConstructor
public class PurchaseRequestController {

	private final PurchaseRequestService purchaseRequestService;

	@GetMapping
	public List<PurchaseRequestResponse> getAll(
		@RequestParam(required = false) String requestNumber,
		@RequestParam(required = false) Long requesterId,
		@RequestParam(required = false) String statusCode,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return purchaseRequestService.findAll(requestNumber, requesterId, statusCode, fromDate, toDate);
	}

	@GetMapping("/{id}")
	public PurchaseRequestResponse getById(@PathVariable Long id) {
		return purchaseRequestService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseRequestResponse create(@RequestBody @Valid PurchaseRequestRequest request) {
		return purchaseRequestService.create(request);
	}

	@PutMapping("/{id}")
	public PurchaseRequestResponse update(@PathVariable Long id,
		@RequestBody @Valid PurchaseRequestRequest request) {
		return purchaseRequestService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		purchaseRequestService.delete(id);
	}

	/** 초안(01)/반려됨(04) → 검토중(02) */
	@PatchMapping("/{id}/submit")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void submit(@PathVariable Long id) {
		purchaseRequestService.submit(id);
	}

	/** 검토중(02) → 승인됨(03) */
	@PostMapping("/{id}/approve")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approve(@PathVariable Long id) {
		purchaseRequestService.approve(id);
	}

	/** 검토중(02) → 반려됨(04) */
	@PostMapping("/{id}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reject(@PathVariable Long id) {
		purchaseRequestService.reject(id);
	}

}
