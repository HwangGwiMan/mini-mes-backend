package com.github.gwiman.mini_mes_backend.materialissue.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaterialIssueLineRequest(
		/** 수정 대상 라인 ID */
		@NotNull Long id,
		/** LOT 번호 — 선택적 */
		String lotNo,
		/** 실제 출고 수량 */
		@NotNull @Positive BigDecimal issuedQty
) {
}
