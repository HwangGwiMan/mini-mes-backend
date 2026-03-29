package com.github.gwiman.mini_mes_backend.routing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 라우팅 공정 단계 엔티티.
 * <p>
 * 라우팅 헤더(Routing) 하나에 속하는 공정 단계 한 행을 나타낸다.
 * stepOrder로 공정 순서를 결정하며, 동일 라우팅 내 같은 processId가 중복되면 안 된다.
 * standardTime은 해당 BOM 기준 표준 작업 시간(분)으로, 공정 마스터의 기본값과 다를 수 있다.
 * </p>
 */
@Entity
@Table(name = "routing_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutingStep extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routing_id", nullable = false)
	private Routing routing;

	@Column(name = "process_id", nullable = false)
	private Long processId;

	@Column(nullable = false)
	private int stepOrder;

	private Integer standardTime;

	@Column(length = 200)
	private String remarks;

	public RoutingStep(Routing routing, Long processId, int stepOrder, Integer standardTime, String remarks) {
		this.routing = routing;
		this.processId = processId;
		this.stepOrder = stepOrder;
		this.standardTime = standardTime;
		this.remarks = remarks;
	}
}
