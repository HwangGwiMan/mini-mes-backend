package com.github.gwiman.mini_mes_backend.bom.internal;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.bom.api.dto.BomLineResponse;
import com.github.gwiman.mini_mes_backend.bom.api.dto.BomResponse;

import lombok.RequiredArgsConstructor;

/**
 * BOM 조회 전용 Repository.
 * <p>
 * jOOQ 코드 생성 전에는 DSL.table/field를 이용한 문자열 기반 쿼리를 사용한다.
 * jOOQ 코드 생성 후에는 타입 안전한 방식으로 교체할 수 있다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class BomQueryRepository {

	private static final org.jooq.Table<Record> BOM = DSL.table("bom");
	private static final org.jooq.Table<Record> BOM_LINE = DSL.table("bom_line");
	private static final org.jooq.Table<Record> ITEM = DSL.table("item");

	private static final Field<Long> B_ID = DSL.field(DSL.name("b", "id"), SQLDataType.BIGINT);
	private static final Field<Long> B_ITEM_ID = DSL.field(DSL.name("b", "item_id"), SQLDataType.BIGINT);
	private static final Field<String> B_VERSION = DSL.field(DSL.name("b", "version"), SQLDataType.VARCHAR);
	private static final Field<java.time.LocalDate> B_VALID_FROM = DSL.field(DSL.name("b", "valid_from"), SQLDataType.LOCALDATE);
	private static final Field<java.time.LocalDate> B_VALID_TO = DSL.field(DSL.name("b", "valid_to"), SQLDataType.LOCALDATE);
	private static final Field<Boolean> B_ACTIVE_YN = DSL.field(DSL.name("b", "active_yn"), SQLDataType.BOOLEAN);

	private static final Field<String> I_CODE = DSL.field(DSL.name("i", "code"), SQLDataType.VARCHAR);
	private static final Field<String> I_NAME = DSL.field(DSL.name("i", "name"), SQLDataType.VARCHAR);

	private static final Field<Long> BL_ID = DSL.field(DSL.name("bl", "id"), SQLDataType.BIGINT);
	private static final Field<Long> BL_BOM_ID = DSL.field(DSL.name("bl", "bom_id"), SQLDataType.BIGINT);
	private static final Field<Long> BL_MATERIAL_ITEM_ID = DSL.field(DSL.name("bl", "material_item_id"), SQLDataType.BIGINT);
	private static final Field<java.math.BigDecimal> BL_QUANTITY = DSL.field(DSL.name("bl", "quantity"), SQLDataType.DECIMAL(19, 4));
	private static final Field<String> BL_UNIT = DSL.field(DSL.name("bl", "unit"), SQLDataType.VARCHAR);
	private static final Field<String> BL_REMARKS = DSL.field(DSL.name("bl", "remarks"), SQLDataType.VARCHAR);
	private static final Field<Integer> BL_SORT_ORDER = DSL.field(DSL.name("bl", "sort_order"), SQLDataType.INTEGER);

	private final DSLContext dsl;

	/**
	 * 검색 조건으로 BOM 목록 조회. 자재 수(line_count)는 서브쿼리로 집계한다.
	 */
	public List<BomResponse> search(String itemCode, String itemName, String version, Boolean activeYn) {
		var b = BOM.as("b");
		var i = ITEM.as("i");

		Field<Integer> lineCount = DSL.field(
			DSL.select(DSL.count())
				.from(BOM_LINE.as("bl_count"))
				.where(DSL.field(DSL.name("bl_count", "bom_id"), SQLDataType.BIGINT).eq(B_ID))
		).as("line_count");

		Condition itemCodeCond = itemCode != null
			? DSL.field(DSL.name("i", "code"), SQLDataType.VARCHAR).like("%" + itemCode + "%")
			: DSL.noCondition();
		Condition itemNameCond = itemName != null
			? DSL.field(DSL.name("i", "name"), SQLDataType.VARCHAR).like("%" + itemName + "%")
			: DSL.noCondition();
		Condition versionCond = version != null ? B_VERSION.like("%" + version + "%") : DSL.noCondition();
		Condition activeCond = activeYn != null ? B_ACTIVE_YN.eq(activeYn) : DSL.noCondition();

		return dsl
			.select(
				B_ID.as("id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("version"),
				B_VALID_FROM.as("valid_from"),
				B_VALID_TO.as("valid_to"),
				B_ACTIVE_YN.as("active_yn"),
				lineCount
			)
			.from(b)
			.join(i).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(itemCodeCond)
			.and(itemNameCond)
			.and(versionCond)
			.and(activeCond)
			.orderBy(I_CODE, B_VERSION.desc())
			.fetch()
			.map(BomResponse::fromRecord);
	}

	/**
	 * 단건 BOM 상세 조회. 헤더 + 자재 라인 + 각 자재의 하위 BOM 존재 여부를 함께 반환한다.
	 */
	public Optional<BomResponse> findByIdWithLines(Long id) {
		var b = BOM.as("b");
		var i = ITEM.as("i");

		Record headerRecord = dsl
			.select(
				B_ID.as("id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("version"),
				B_VALID_FROM.as("valid_from"),
				B_VALID_TO.as("valid_to"),
				B_ACTIVE_YN.as("active_yn"),
				DSL.val(0).as("line_count")
			)
			.from(b)
			.join(i).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(B_ID.eq(id))
			.fetchOne();

		if (headerRecord == null) {
			return Optional.empty();
		}

		var bl = BOM_LINE.as("bl");
		var mi = ITEM.as("mi");

		var lineRecords = dsl
			.select(
				BL_ID.as("id"),
				BL_MATERIAL_ITEM_ID.as("material_item_id"),
				DSL.field(DSL.name("mi", "code"), SQLDataType.VARCHAR).as("material_item_code"),
				DSL.field(DSL.name("mi", "name"), SQLDataType.VARCHAR).as("material_item_name"),
				BL_QUANTITY.as("quantity"),
				BL_UNIT.as("unit"),
				BL_REMARKS.as("remarks"),
				BL_SORT_ORDER.as("sort_order")
			)
			.from(bl)
			.join(mi).on(BL_MATERIAL_ITEM_ID.eq(DSL.field(DSL.name("mi", "id"), SQLDataType.BIGINT)))
			.where(BL_BOM_ID.eq(id))
			.orderBy(BL_SORT_ORDER)
			.fetch();

		// 자재 품목들의 하위 BOM 존재 여부를 IN 쿼리 한 번으로 확인 — N+1 방지
		Set<Long> materialItemIds = lineRecords.stream()
			.map(r -> r.get("material_item_id", Long.class))
			.collect(Collectors.toSet());

		Set<Long> itemIdsWithBom = materialItemIds.isEmpty() ? Set.of() : dsl
			.selectDistinct(DSL.field(DSL.name("item_id"), SQLDataType.BIGINT))
			.from(BOM)
			.where(DSL.field(DSL.name("item_id"), SQLDataType.BIGINT).in(materialItemIds))
			.fetchSet(DSL.field(DSL.name("item_id"), SQLDataType.BIGINT));

		List<BomLineResponse> lines = lineRecords.stream()
			.map(r -> BomLineResponse.fromRecord(r, itemIdsWithBom.contains(r.get("material_item_id", Long.class))))
			.toList();

		return Optional.of(BomResponse.fromRecord(headerRecord, lines));
	}

	/**
	 * 특정 품목의 BOM 목록 조회 — 버전 이력 확인용.
	 */
	public List<BomResponse> findByItemId(Long itemId) {
		var b = BOM.as("b");
		var i = ITEM.as("i");

		Field<Integer> lineCount = DSL.field(
			DSL.select(DSL.count())
				.from(BOM_LINE.as("bl_count"))
				.where(DSL.field(DSL.name("bl_count", "bom_id"), SQLDataType.BIGINT).eq(B_ID))
		).as("line_count");

		return dsl
			.select(
				B_ID.as("id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("version"),
				B_VALID_FROM.as("valid_from"),
				B_VALID_TO.as("valid_to"),
				B_ACTIVE_YN.as("active_yn"),
				lineCount
			)
			.from(b)
			.join(i).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(B_ITEM_ID.eq(itemId))
			.orderBy(B_VERSION.desc())
			.fetch()
			.map(BomResponse::fromRecord);
	}
}
