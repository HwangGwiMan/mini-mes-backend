package com.github.gwiman.mini_mes_backend.partner.internal;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Partner;
import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PartnerQueryRepository {

	private final DSLContext dsl;

	public Optional<PartnerResponse> findById(Long id) {
		Partner p = Partner.PARTNER;
		return dsl
			.selectFrom(p)
			.where(p.ID.eq(id))
			.fetchOptional()
			.map(PartnerResponse::fromRecord);
	}
}
