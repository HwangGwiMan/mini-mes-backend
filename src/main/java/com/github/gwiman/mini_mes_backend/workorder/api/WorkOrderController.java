package com.github.gwiman.mini_mes_backend.workorder.api;

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

import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderRequest;
import com.github.gwiman.mini_mes_backend.workorder.api.dto.WorkOrderResponse;
import com.github.gwiman.mini_mes_backend.workorder.application.WorkOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

	private final WorkOrderService workOrderService;

	@GetMapping
	public List<WorkOrderResponse> getAll(
			@RequestParam(required = false) String workOrderNumber,
			@RequestParam(required = false) String itemName,
			@RequestParam(required = false) String statusCode) {
		return workOrderService.findAll(workOrderNumber, itemName, statusCode);
	}

	@GetMapping("/{id}")
	public WorkOrderResponse getById(@PathVariable Long id) {
		return workOrderService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkOrderResponse create(@RequestBody @Valid WorkOrderRequest request) {
		return workOrderService.create(request);
	}

	@PutMapping("/{id}")
	public WorkOrderResponse update(
			@PathVariable Long id,
			@RequestBody @Valid WorkOrderRequest request) {
		return workOrderService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		workOrderService.delete(id);
	}

	@PatchMapping("/{id}/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirm(@PathVariable Long id) {
		workOrderService.confirm(id);
	}

	@PatchMapping("/{id}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable Long id) {
		workOrderService.cancel(id);
	}
}
