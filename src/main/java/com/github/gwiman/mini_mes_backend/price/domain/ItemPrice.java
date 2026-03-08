package com.github.gwiman.mini_mes_backend.price.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 품목별 기준 판매단가 엔티티.
 * 품목당 단가는 1개만 등록 가능하며(item_id UNIQUE),
 * 거래처 구분 없는 단순 기준단가를 관리한다.
 */
@Entity
@Table(name = "item_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "item_id", nullable = false, unique = true)
	private Long itemId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal unitPrice;

	@Column(length = 200)
	private String remarks;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public ItemPrice(Long itemId, BigDecimal unitPrice, String remarks) {
		this.itemId = itemId;
		this.unitPrice = unitPrice;
		this.remarks = remarks;
	}

	public void update(BigDecimal unitPrice, String remarks) {
		this.unitPrice = unitPrice;
		this.remarks = remarks;
	}
}
