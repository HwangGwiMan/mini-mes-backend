package com.github.gwiman.mini_mes_backend.partner.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 20)
	private String businessNumber;

	@Column(length = 50)
	private String ceoName;

	@Column(length = 200)
	private String address;

	@Column(length = 20)
	private String phone1;

	@Column(length = 20)
	private String phone2;

	@Column(length = 20)
	private String tradeTypeCode;

	public Partner(String code, String name, String businessNumber,
		String ceoName, String address, String phone1, String phone2, String tradeTypeCode) {
		this.code = code;
		this.name = name;
		this.businessNumber = businessNumber;
		this.ceoName = ceoName;
		this.address = address;
		this.phone1 = phone1;
		this.phone2 = phone2;
		this.tradeTypeCode = tradeTypeCode;
	}

	public void update(String code, String name, String businessNumber,
		String ceoName, String address, String phone1, String phone2, String tradeTypeCode) {
		this.code = code;
		this.name = name;
		this.businessNumber = businessNumber;
		this.ceoName = ceoName;
		this.address = address;
		this.phone1 = phone1;
		this.phone2 = phone2;
		this.tradeTypeCode = tradeTypeCode;
	}
}
