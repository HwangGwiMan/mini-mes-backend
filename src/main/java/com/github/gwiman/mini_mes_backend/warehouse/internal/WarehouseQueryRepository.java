package com.github.gwiman.mini_mes_backend.warehouse.internal;

import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.warehouse.api.dto.WarehouseResponse;

import lombok.RequiredArgsConstructor;

/** warehouse 테이블 단건 조회 — jOOQ codegen 미실행 상태이므로 DSL.table 패턴 사용 */
@Component
@RequiredArgsConstructor
public class WarehouseQueryRepository {

	private static final org.jooq.Table<Record> WAREHOUSE = DSL.table("warehouse");

	private final DSLContext dsl;

	public Optional<WarehouseResponse> findById(Long id) {
		return dsl
			.selectFrom(WAREHOUSE)
			.where(DSL.field(DSL.name("id"), SQLDataType.BIGINT).eq(id))
			.fetchOptional()
			.map(WarehouseResponse::fromRecord);
	}
}
