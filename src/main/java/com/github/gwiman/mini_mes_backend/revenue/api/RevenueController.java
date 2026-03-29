package com.github.gwiman.mini_mes_backend.revenue.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.revenue.api.dto.AvailableOrderLineResponse;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueCreateRequest;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueResponse;
import com.github.gwiman.mini_mes_backend.revenue.api.dto.RevenueUpdateRequest;
import com.github.gwiman.mini_mes_backend.revenue.application.RevenueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 매출 관리 API 컨트롤러.
 * 매출 CRUD, 마감/취소 처리, 품목 선택 팝업용 수주 라인 조회 엔드포인트를 제공한다.
 */
@RestController
@RequestMapping("/api/revenues")
@RequiredArgsConstructor
public class RevenueController {

	private final RevenueService revenueService;

	@GetMapping
	public List<RevenueResponse> findAll(
		@RequestParam(required = false) String statusCode,
		@RequestParam(required = false) Long partnerId,
		@RequestParam(required = false) Long salesOrderId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return revenueService.findAll(statusCode, partnerId, salesOrderId, fromDate, toDate);
	}

	/** 매출 생성 시 거래처 완료 수주 품목 선택 팝업용 — /available-lines 경로가 /{id} 보다 먼저 매핑되어야 한다 */
	@GetMapping("/available-lines")
	public List<AvailableOrderLineResponse> findAvailableOrderLines(
		@RequestParam Long partnerId) {
		return revenueService.findAvailableOrderLines(partnerId);
	}

	@GetMapping("/{id}")
	public RevenueResponse findById(@PathVariable Long id) {
		return revenueService.findById(id);
	}

	@PostMapping
	public RevenueResponse create(@RequestBody @Valid RevenueCreateRequest request) {
		return revenueService.create(request);
	}

	@PutMapping("/{id}")
	public RevenueResponse update(@PathVariable Long id,
		@RequestBody @Valid RevenueUpdateRequest request) {
		return revenueService.update(id, request);
	}

	/** 매출 마감: 초안 → 마감 */
	@PostMapping("/{id}/close")
	public RevenueResponse close(@PathVariable Long id) {
		return revenueService.close(id);
	}

	/** 마감 취소: 마감 → 취소 */
	@PostMapping("/{id}/cancel")
	public RevenueResponse cancel(@PathVariable Long id) {
		return revenueService.cancel(id);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		revenueService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
