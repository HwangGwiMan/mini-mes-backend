package com.github.gwiman.mini_mes_backend.salesorder.domain;

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

@Entity
@Table(name = "sales_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesOrder extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String orderNumber;

	@Column(nullable = false)
	private LocalDate orderDate;

	private LocalDate deliveryDate;

	@Column(name = "partner_id", nullable = false)
	private Long partnerId;

	@Column(name = "employee_id")
	private Long employeeId;

	@Column(name = "quote_id")
	private Long quoteId;

	@Column(length = 20)
	private String statusCode;

	@Column(length = 200)
	private String remarks;

	@OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<SalesOrderLine> lines = new ArrayList<>();

	private SalesOrder(String orderNumber, LocalDate orderDate, LocalDate deliveryDate,
		Long partnerId, Long employeeId, Long quoteId, String statusCode, String remarks) {
		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.deliveryDate = deliveryDate;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.quoteId = quoteId;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	/** 직접 수주 생성 — 상태 코드는 요청값을 그대로 사용 */
	public static SalesOrder create(String orderNumber, LocalDate orderDate, LocalDate deliveryDate,
		Long partnerId, Long employeeId, Long quoteId, String statusCode, String remarks) {
		return new SalesOrder(orderNumber, orderDate, deliveryDate,
			partnerId, employeeId, quoteId,
			statusCode != null ? statusCode : "",
			remarks != null ? remarks : "");
	}

	/** 견적 전환 수주 생성 — 수주일은 오늘, 초기 상태는 항상 접수(ORDER_STATUS_01) */
	public static SalesOrder fromQuote(String orderNumber, Long quoteId,
		Long partnerId, Long employeeId) {
		return new SalesOrder(orderNumber, LocalDate.now(), null,
			partnerId, employeeId, quoteId, "ORDER_STATUS_01", "");
	}

	public void update(LocalDate orderDate, LocalDate deliveryDate,
		Long partnerId, Long employeeId, String statusCode, String remarks) {
		this.orderDate = orderDate;
		this.deliveryDate = deliveryDate;
		this.partnerId = partnerId;
		this.employeeId = employeeId;
		this.statusCode = statusCode;
		this.remarks = remarks;
	}

	public void addLine(SalesOrderLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}
}
