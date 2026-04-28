package com.github.gwiman.mini_mes_backend.materialissue.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record MaterialIssueRequest(
		/** 연결 작업지시 ID — 생성 시 필수, 수정 시 무시 */
		Long workOrderId,
		@NotNull LocalDate issueDate,
		String remarks,
		/** 수정 시 라인별 LOT/수량 갱신 목록 — 생성 시 null 허용 */
		List<MaterialIssueLineRequest> lines
) {
}
