package com.github.gwiman.mini_mes_backend.shipment.api;

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

import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentCompleteRequest;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentResponse;
import com.github.gwiman.mini_mes_backend.shipment.api.dto.ShipmentUpdateRequest;
import com.github.gwiman.mini_mes_backend.shipment.application.ShipmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 출하 REST 컨트롤러.
 * 출하 계획 조회/수정/삭제 및 출하 완료 처리 엔드포인트를 제공한다.
 * 출하 생성은 수주 등록 시 자동으로 이루어지므로 POST 엔드포인트는 제공하지 않는다.
 */
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

	private final ShipmentService shipmentService;

	@GetMapping
	public List<ShipmentResponse> getAll(
		@RequestParam(required = false) String statusCode,
		@RequestParam(required = false) Long salesOrderId,
		@RequestParam(required = false) Long partnerId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return shipmentService.findAll(statusCode, salesOrderId, partnerId, fromDate, toDate);
	}

	@GetMapping("/{id}")
	public ShipmentResponse getById(@PathVariable Long id) {
		return shipmentService.findById(id);
	}

	@PutMapping("/{id}")
	public ShipmentResponse update(@PathVariable Long id, @RequestBody @Valid ShipmentUpdateRequest request) {
		return shipmentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		shipmentService.delete(id);
	}

	@PostMapping("/{id}/complete")
	public ShipmentResponse complete(@PathVariable Long id, @RequestBody @Valid ShipmentCompleteRequest request) {
		return shipmentService.complete(id, request);
	}
}
