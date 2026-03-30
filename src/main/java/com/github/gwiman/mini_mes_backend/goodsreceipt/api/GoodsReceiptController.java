package com.github.gwiman.mini_mes_backend.goodsreceipt.api;

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

import com.github.gwiman.mini_mes_backend.goodsreceipt.application.GoodsReceiptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

	private final GoodsReceiptService goodsReceiptService;

	@GetMapping
	public List<GoodsReceiptResponse> getAll(
			@RequestParam(required = false) String receiptNumber,
			@RequestParam(required = false) String partnerName,
			@RequestParam(required = false) String statusCode) {
		return goodsReceiptService.findAll(receiptNumber, partnerName, statusCode);
	}

	@GetMapping("/{id}")
	public GoodsReceiptResponse getById(@PathVariable Long id) {
		return goodsReceiptService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public GoodsReceiptResponse create(@RequestBody @Valid GoodsReceiptRequest request) {
		return goodsReceiptService.create(request);
	}

	@PutMapping("/{id}")
	public GoodsReceiptResponse update(
			@PathVariable Long id,
			@RequestBody @Valid GoodsReceiptRequest request) {
		return goodsReceiptService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		goodsReceiptService.delete(id);
	}

	@PatchMapping("/{id}/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirm(@PathVariable Long id) {
		goodsReceiptService.confirm(id);
	}

	@PatchMapping("/{id}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable Long id) {
		goodsReceiptService.cancel(id);
	}
}
