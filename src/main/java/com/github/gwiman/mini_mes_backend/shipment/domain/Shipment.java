package com.github.gwiman.mini_mes_backend.shipment.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출하 헤더 엔티티.
 * 수주 등록 시 자동으로 생성되며, SHIPMENT_STATUS 공통코드로 상태를 관리한다.
 * 출하 계획(출하대기/출하중) → 출하 완료 흐름으로 처리된다.
 */
@Entity
@Table(name = "shipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 자동채번: SH_YYYYMM_001 */
	@Column(unique = true, nullable = false, length = 50)
	private String shipmentNumber;

	/** 연결된 수주 ID (필수) */
	@Column(name = "sales_order_id", nullable = false)
	private Long salesOrderId;

	/** 실출하일자 — 출하 완료 처리 시 입력 */
	private LocalDate shipmentDate;

	@Column(name = "partner_id", nullable = false)
	private Long partnerId;

	@Column(name = "employee_id")
	private Long employeeId;

	/** SHIPMENT_STATUS 공통코드 참조 */
	@Column(length = 20, nullable = false)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<ShipmentLine> lines = new ArrayList<>();

	public Shipment(String shipmentNumber, Long salesOrderId, Long partnerId,
		Long employeeId, String statusCode, String remarks) {
		this.shipmentNumber = shipmentNumber;
		this.salesOrderId = salesOrderId;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	/** 출하 계획 수정 — 출하대기/출하중 상태에서만 허용 */
	public void update(Long employeeId, String statusCode, String remarks) {
		this.employeeId = employeeId;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	/** 출하 완료 처리 — 실출하일자를 기록하고 상태를 출하완료로 변경 */
	public void complete(LocalDate shipmentDate) {
		this.shipmentDate = shipmentDate;
		this.statusCode = "SHIPMENT_STATUS_03";
	}

	public void addLine(ShipmentLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}
}
