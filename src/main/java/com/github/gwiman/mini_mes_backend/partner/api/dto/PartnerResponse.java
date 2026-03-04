package com.github.gwiman.mini_mes_backend.partner.api.dto;

import org.jooq.Record;

import com.github.gwiman.mini_mes_backend.partner.domain.Partner;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PartnerResponse {

	private Long id;
	private String code;
	private String name;
	private String businessNumber;
	private String ceoName;
	private String address;
	private String phone1;
	private String phone2;
	private String tradeTypeCode;

	public PartnerResponse(Long id, String code, String name,
		String businessNumber, String ceoName, String address,
		String phone1, String phone2, String tradeTypeCode) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.businessNumber = businessNumber;
		this.ceoName = ceoName;
		this.address = address;
		this.phone1 = phone1;
		this.phone2 = phone2;
		this.tradeTypeCode = tradeTypeCode;
	}

	public static PartnerResponse from(Partner entity) {
		return new PartnerResponse(
			entity.getId(),
			entity.getCode(),
			entity.getName(),
			entity.getBusinessNumber(),
			entity.getCeoName(),
			entity.getAddress(),
			entity.getPhone1(),
			entity.getPhone2(),
			entity.getTradeTypeCode()
		);
	}

	public static PartnerResponse fromRecord(Record r) {
		com.github.gwiman.mini_mes_backend.jooq.tables.Partner p = com.github.gwiman.mini_mes_backend.jooq.tables.Partner.PARTNER;
		return new PartnerResponse(
			r.get(p.ID),
			r.get(p.CODE),
			r.get(p.NAME),
			r.get(p.BUSINESS_NUMBER),
			r.get(p.CEO_NAME),
			r.get(p.ADDRESS),
			r.get(p.PHONE1),
			r.get(p.PHONE2),
			r.get(p.TRADE_TYPE_CODE)
		);
	}
}
