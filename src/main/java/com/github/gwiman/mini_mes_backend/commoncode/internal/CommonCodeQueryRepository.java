package com.github.gwiman.mini_mes_backend.commoncode.internal;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.CommonCode;
import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommonCodeQueryRepository {

	private final DSLContext dsl;

	public Optional<CommonCodeResponse> findById(Long id) {
		CommonCode c = CommonCode.COMMON_CODE;
		return dsl
			.selectFrom(c)
			.where(c.ID.eq(id))
			.fetchOptional()
			.map(CommonCodeResponse::fromRecord);
	}
}
