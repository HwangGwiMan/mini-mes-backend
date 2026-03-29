package com.github.gwiman.mini_mes_backend.shipment.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 출하 헤더 응답 DTO.
 * name 필드는 useCrudPage 호환용으로 shipmentNumber와 동일한 값을 가진다.
 */
public record ShipmentResponse(
	Long id,
	String shipmentNumber,
	String name, // useCrudPage 호환용 (shipmentNumber와 동일)
	Long salesOrderId,
	String salesOrderNumber,
	LocalDate shipmentDate,
	Long partnerId,
	String partnerName,
	Long employeeId,
	String employeeName,
	String statusCode,
	String remarks,
	List<ShipmentLineResponse> lines
) {}
