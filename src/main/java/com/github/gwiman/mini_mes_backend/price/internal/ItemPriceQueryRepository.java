package com.github.gwiman.mini_mes_backend.price.internal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.price.api.dto.ItemPriceResponse;

import lombok.RequiredArgsConstructor;

/**
 * 단가 조회 전용 리포지토리 (jOOQ raw DSL).
 * item_price 테이블은 신규 테이블이라 jOOQ 생성 클래스가 없으므로
 * DSL.table/field 로 직접 참조하고, item 테이블은 기존 생성 클래스를 JOIN에 활용한다.
 */
@Component
@RequiredArgsConstructor
public class ItemPriceQueryRepository {

	// item_price 테이블 raw DSL 상수
	private static final Table<Record> IP = DSL.table(DSL.name("item_price")).as("ip");
	private static final Field<Long> IP_ID =
		DSL.field(DSL.name("ip", "id"), SQLDataType.BIGINT).as("ip_id");
	private static final Field<Long> IP_ITEM_ID =
		DSL.field(DSL.name("ip", "item_id"), SQLDataType.BIGINT).as("ip_item_id");
	private static final Field<BigDecimal> IP_UNIT_PRICE =
		DSL.field(DSL.name("ip", "unit_price"), SQLDataType.NUMERIC(19, 2)).as("ip_unit_price");
	private static final Field<String> IP_REMARKS =
		DSL.field(DSL.name("ip", "remarks"), SQLDataType.VARCHAR(200)).as("ip_remarks");
	private static final Field<LocalDateTime> IP_UPDATED_AT =
		DSL.field(DSL.name("ip", "updated_at"), SQLDataType.LOCALDATETIME).as("ip_updated_at");

	// WHERE 조건용 (alias 없는 원본 필드)
	private static final Field<Long> IP_ID_RAW =
		DSL.field(DSL.name("ip", "id"), SQLDataType.BIGINT);
	private static final Field<Long> IP_ITEM_ID_RAW =
		DSL.field(DSL.name("ip", "item_id"), SQLDataType.BIGINT);

	private final DSLContext dsl;

	public List<ItemPriceResponse> search(String itemCodePattern, String itemNamePattern) {
		Item i = Item.ITEM;

		Condition codeCond = itemCodePattern != null
			? i.CODE.like(itemCodePattern)
			: DSL.noCondition();
		Condition nameCond = itemNamePattern != null
			? i.NAME.like(itemNamePattern)
			: DSL.noCondition();

		return dsl
			.select(IP_ID, IP_ITEM_ID, IP_UNIT_PRICE, IP_REMARKS, IP_UPDATED_AT,
				i.CODE, i.NAME)
			.from(IP)
			.join(i).on(IP_ITEM_ID_RAW.eq(i.ID))
			.where(codeCond)
			.and(nameCond)
			.orderBy(i.CODE.asc())
			.fetch()
			.map(ItemPriceResponse::fromRecord);
	}

	public Optional<ItemPriceResponse> findById(Long id) {
		Item i = Item.ITEM;

		return dsl
			.select(IP_ID, IP_ITEM_ID, IP_UNIT_PRICE, IP_REMARKS, IP_UPDATED_AT,
				i.CODE, i.NAME)
			.from(IP)
			.join(i).on(IP_ITEM_ID_RAW.eq(i.ID))
			.where(IP_ID_RAW.eq(id))
			.fetchOptional()
			.map(ItemPriceResponse::fromRecord);
	}
}
