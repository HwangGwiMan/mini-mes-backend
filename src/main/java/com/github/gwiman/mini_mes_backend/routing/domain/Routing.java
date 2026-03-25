package com.github.gwiman.mini_mes_backend.routing.domain;

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
 * 라우팅 헤더 엔티티.
 * <p>
 * 특정 BOM에 대해 생산에 필요한 공정 순서를 정의한다.
 * BOM 1개에 라우팅 1개가 대응되므로 bomId는 UNIQUE 제약을 가진다. (ADR-002)
 * 삭제 대신 비활성(activeYn = false) 처리로 이력을 보존한다.
 * </p>
 */
@Entity
@Table(name = "routing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Routing extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bom_id", nullable = false, unique = true)
	private Long bomId;

	@Column(nullable = false)
	private Boolean activeYn = true;

	@OneToMany(mappedBy = "routing", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<RoutingStep> steps = new ArrayList<>();

	public Routing(Long bomId) {
		this.bomId = bomId;
		this.activeYn = true;
	}

	public void deactivate() {
		this.activeYn = false;
	}

	public void addStep(RoutingStep step) {
		steps.add(step);
	}

	public void clearSteps() {
		steps.clear();
	}
}
