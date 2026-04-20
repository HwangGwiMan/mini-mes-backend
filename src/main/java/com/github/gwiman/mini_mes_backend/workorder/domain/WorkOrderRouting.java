package com.github.gwiman.mini_mes_backend.workorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작업지시 공정 라우팅 라인 엔티티.
 * <p>
 * 작업지시 생성 시 BOM에 연결된 라우팅을 전개하여 생성되는 스냅샷 레코드.
 * 라우팅 마스터 변경에 영향받지 않도록 생성 시점의 공정 순서를 보존한다.
 * </p>
 */
@Entity
@Table(name = "work_order_routing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkOrderRouting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_order_id", nullable = false)
	private WorkOrder workOrder;

	/** 원본 Routing 헤더 ID (스냅샷) */
	@Column(name = "routing_id", nullable = false)
	private Long routingId;

	/** 공정 ID (스냅샷) */
	@Column(name = "process_id", nullable = false)
	private Long processId;

	/** 공정 순서 */
	@Column(nullable = false)
	private int stepOrder;

	/** 표준 작업 시간(분) — null 허용 */
	private Integer standardTime;

	@Column(length = 200)
	private String remarks;

	private WorkOrderRouting(WorkOrder workOrder, Long routingId, Long processId,
			int stepOrder, Integer standardTime, String remarks) {
		this.workOrder = workOrder;
		this.routingId = routingId;
		this.processId = processId;
		this.stepOrder = stepOrder;
		this.standardTime = standardTime;
		this.remarks = remarks;
	}

	public static WorkOrderRouting of(WorkOrder workOrder, Long routingId, Long processId,
			int stepOrder, Integer standardTime, String remarks) {
		return new WorkOrderRouting(workOrder, routingId, processId, stepOrder, standardTime, remarks);
	}
}
