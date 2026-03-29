package com.github.gwiman.mini_mes_backend.bom.domain;

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
import jakarta.persistence.UniqueConstraint;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BOM 헤더 엔티티.
 * <p>
 * 특정 품목(itemId)을 1개 생산하기 위해 필요한 자재 목록을 정의한다.
 * 동일 품목에 대해 여러 버전의 BOM을 관리할 수 있으며, (itemId, version) 조합은 유일해야 한다.
 * 삭제 대신 비활성(activeYn = false) 처리로 이력을 보존한다.
 * </p>
 */
@Entity
@Table(name = "bom", uniqueConstraints = {
	@UniqueConstraint(name = "uk_bom_item_version", columnNames = {"item_id", "version"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "item_id", nullable = false)
	private Long itemId;

	@Column(nullable = false, length = 20)
	private String version;

	private LocalDate validFrom;

	private LocalDate validTo;

	@Column(nullable = false)
	private Boolean activeYn = true;

	@OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<BomLine> lines = new ArrayList<>();

	public Bom(Long itemId, String version, LocalDate validFrom, LocalDate validTo) {
		this.itemId = itemId;
		this.version = version;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.activeYn = true;
	}

	public void update(LocalDate validFrom, LocalDate validTo) {
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public void deactivate() {
		this.activeYn = false;
	}

	public void addLine(BomLine line) {
		lines.add(line);
	}

	public void clearLines() {
		lines.clear();
	}
}
