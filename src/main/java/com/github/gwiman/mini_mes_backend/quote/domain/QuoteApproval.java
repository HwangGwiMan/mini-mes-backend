package com.github.gwiman.mini_mes_backend.quote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.github.gwiman.mini_mes_backend.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quote_approval")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteApproval extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long quoteId;

	@Column(nullable = false)
	private Long approverEmployeeId;

	@Column(nullable = false)
	private String approverUsername;

	@Column(nullable = false)
	private String approverName;

	@Column(nullable = false, length = 20)
	private String action; // "APPROVED" | "REJECTED"

	@Column(length = 500)
	private String comment;

	public QuoteApproval(Long quoteId, Long approverEmployeeId, String approverUsername,
		String approverName, String action, String comment) {
		this.quoteId = quoteId;
		this.approverEmployeeId = approverEmployeeId;
		this.approverUsername = approverUsername;
		this.approverName = approverName;
		this.action = action;
		this.comment = comment;
	}
}
