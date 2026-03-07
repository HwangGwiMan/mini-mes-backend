package com.github.gwiman.mini_mes_backend.quote.api.dto;

import java.time.LocalDateTime;

import com.github.gwiman.mini_mes_backend.quote.domain.QuoteApproval;

public record ApprovalResponse(
	Long id,
	Long quoteId,
	Long approverEmployeeId,
	String approverUsername,
	String approverName,
	String action,
	String comment,
	LocalDateTime createdAt
) {
	public static ApprovalResponse from(QuoteApproval entity) {
		return new ApprovalResponse(
			entity.getId(),
			entity.getQuoteId(),
			entity.getApproverEmployeeId(),
			entity.getApproverUsername(),
			entity.getApproverName(),
			entity.getAction(),
			entity.getComment(),
			entity.getCreatedAt()
		);
	}
}
