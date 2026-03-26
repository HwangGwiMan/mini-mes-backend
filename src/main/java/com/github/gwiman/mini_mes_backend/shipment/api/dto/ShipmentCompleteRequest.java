package com.github.gwiman.mini_mes_backend.shipment.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 출하 완료 처리 요청 DTO.
 * 실출하일자와 라인별 실출하수량을 입력받는다.
 * 부분 출하를 허용하므로 실출하수량이 계획수량보다 적어도 된다.
 */
public record ShipmentCompleteRequest(
	@NotNull(message = "출하일자는 필수입니다.")
	LocalDate shipmentDate,

	@NotEmpty(message = "출하 라인은 최소 1건 이상이어야 합니다.")
	@Valid
	List<LineItem> lines
) {
	public record LineItem(
		@NotNull(message = "라인 ID는 필수입니다.") Long id,
		@NotNull(message = "실출하수량은 필수입니다.") BigDecimal actualQuantity
	) {}
}
