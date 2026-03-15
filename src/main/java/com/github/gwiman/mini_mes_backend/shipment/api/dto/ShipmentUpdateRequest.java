package com.github.gwiman.mini_mes_backend.shipment.api.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 출하 계획 수정 요청 DTO.
 * 출하대기/출하중 상태에서만 허용되며, 계획수량과 상태를 변경할 수 있다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ShipmentUpdateRequest {

	private Long employeeId;

	@NotNull(message = "상태코드는 필수입니다.")
	private String statusCode;

	private String remarks;

	@NotEmpty(message = "출하 라인은 최소 1건 이상이어야 합니다.")
	@Valid
	private List<LineItem> lines;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class LineItem {
		@NotNull(message = "라인 ID는 필수입니다.")
		private Long id;

		@NotNull(message = "계획수량은 필수입니다.")
		private BigDecimal plannedQuantity;

		private String remarks;
	}
}
