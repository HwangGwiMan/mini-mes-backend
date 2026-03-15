package com.github.gwiman.mini_mes_backend.shipment.api.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출하 헤더 응답 DTO.
 * name 필드는 useCrudPage 호환용으로 shipmentNumber와 동일한 값을 가진다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

	private Long id;
	private String shipmentNumber;
	private String name; // useCrudPage 호환용 (shipmentNumber와 동일)
	private Long salesOrderId;
	private String salesOrderNumber;
	private LocalDate shipmentDate;
	private Long partnerId;
	private String partnerName;
	private Long employeeId;
	private String employeeName;
	private String statusCode;
	private String remarks;
	private List<ShipmentLineResponse> lines;
}
