package com.github.gwiman.mini_mes_backend.orderfulfillment.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.orderfulfillment.api.dto.OrderFulfillmentResponse;
import com.github.gwiman.mini_mes_backend.orderfulfillment.infrastructure.OrderFulfillmentQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 수주이행현황 조회 API 컨트롤러.
 * 수주·출하·매출 데이터를 집계한 현황을 읽기 전용으로 제공한다.
 * 별도의 서비스 레이어 없이 QueryRepository를 직접 호출한다 — 비즈니스 로직이 없는 순수 조회이기 때문이다.
 */
@RestController
@RequestMapping("/api/order-fulfillment")
@RequiredArgsConstructor
public class OrderFulfillmentController {

	private final OrderFulfillmentQueryRepository queryRepository;

	@GetMapping
	public List<OrderFulfillmentResponse> findAll(
		@RequestParam(required = false) String orderNumber,
		@RequestParam(required = false) Long partnerId,
		@RequestParam(required = false) String orderStatusCode,
		@RequestParam(required = false) String shipmentStatusCode,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

		String orderNumberPattern = QueryParamEscaper.containsLike(orderNumber);
		return queryRepository.search(orderNumberPattern, partnerId, orderStatusCode, shipmentStatusCode, fromDate, toDate);
	}
}
