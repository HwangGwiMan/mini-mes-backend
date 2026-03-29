package com.github.gwiman.mini_mes_backend.purchaserequest.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 구매 요청 응답 DTO.
 * name 필드는 useCrudPage 호환을 위해 requestNumber와 동일한 값을 담는다.
 */
public record PurchaseRequestResponse(
	Long id,
	String requestNumber,
	String name,
	LocalDate requestDate,
	Long requesterId,
	String requesterName,
	String statusCode,
	String remarks,
	List<PurchaseRequestLineResponse> lines
) {}
