package com.github.gwiman.mini_mes_backend.routing.internal;

import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingResponse;
import com.github.gwiman.mini_mes_backend.routing.api.dto.RoutingStepResponse;

import lombok.RequiredArgsConstructor;

/**
 * 라우팅 조회 전용 Repository.
 * <p>
 * jOOQ 코드 생성 전에는 DSL.table/field를 이용한 문자열 기반 쿼리를 사용한다.
 * routing → bom → item 조인으로 완제품 정보를 함께 조회한다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RoutingQueryRepository {

	private static final org.jooq.Table<Record> ROUTING = DSL.table("routing");
	private static final org.jooq.Table<Record> ROUTING_STEP = DSL.table("routing_step");
	private static final org.jooq.Table<Record> BOM = DSL.table("bom");
	private static final org.jooq.Table<Record> ITEM = DSL.table("item");
	private static final org.jooq.Table<Record> PROCESS = DSL.table("process");

	private static final Field<Long> R_ID = DSL.field(DSL.name("r", "id"), SQLDataType.BIGINT);
	private static final Field<Long> R_BOM_ID = DSL.field(DSL.name("r", "bom_id"), SQLDataType.BIGINT);
	private static final Field<Boolean> R_ACTIVE_YN = DSL.field(DSL.name("r", "active_yn"), SQLDataType.BOOLEAN);

	private static final Field<Long> B_ID = DSL.field(DSL.name("b", "id"), SQLDataType.BIGINT);
	private static final Field<Long> B_ITEM_ID = DSL.field(DSL.name("b", "item_id"), SQLDataType.BIGINT);
	private static final Field<String> B_VERSION = DSL.field(DSL.name("b", "version"), SQLDataType.VARCHAR);

	private static final Field<String> I_CODE = DSL.field(DSL.name("i", "code"), SQLDataType.VARCHAR);
	private static final Field<String> I_NAME = DSL.field(DSL.name("i", "name"), SQLDataType.VARCHAR);

	private static final Field<Long> RS_ID = DSL.field(DSL.name("rs", "id"), SQLDataType.BIGINT);
	private static final Field<Long> RS_ROUTING_ID = DSL.field(DSL.name("rs", "routing_id"), SQLDataType.BIGINT);
	private static final Field<Long> RS_PROCESS_ID = DSL.field(DSL.name("rs", "process_id"), SQLDataType.BIGINT);
	private static final Field<Integer> RS_STEP_ORDER = DSL.field(DSL.name("rs", "step_order"), SQLDataType.INTEGER);
	private static final Field<Integer> RS_STANDARD_TIME = DSL.field(DSL.name("rs", "standard_time"), SQLDataType.INTEGER);
	private static final Field<String> RS_REMARKS = DSL.field(DSL.name("rs", "remarks"), SQLDataType.VARCHAR);

	private final DSLContext dsl;

	/**
	 * 검색 조건으로 라우팅 목록 조회. 공정 수(step_count)는 서브쿼리로 집계한다.
	 */
	public List<RoutingResponse> search(String itemCode, String itemName, String bomVersion, Boolean activeYn) {
		Field<Integer> stepCount = DSL.field(
			DSL.select(DSL.count())
				.from(ROUTING_STEP.as("rs_count"))
				.where(DSL.field(DSL.name("rs_count", "routing_id"), SQLDataType.BIGINT).eq(R_ID))
		).as("step_count");

		Condition itemCodeCond = itemCode != null
			? DSL.field(DSL.name("i", "code"), SQLDataType.VARCHAR).like("%" + itemCode + "%")
			: DSL.noCondition();
		Condition itemNameCond = itemName != null
			? DSL.field(DSL.name("i", "name"), SQLDataType.VARCHAR).like("%" + itemName + "%")
			: DSL.noCondition();
		Condition versionCond = bomVersion != null
			? B_VERSION.like("%" + bomVersion + "%")
			: DSL.noCondition();
		Condition activeCond = activeYn != null ? R_ACTIVE_YN.eq(activeYn) : DSL.noCondition();

		return dsl
			.select(
				R_ID.as("id"),
				R_BOM_ID.as("bom_id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("bom_version"),
				R_ACTIVE_YN.as("active_yn"),
				stepCount
			)
			.from(ROUTING.as("r"))
			.join(BOM.as("b")).on(R_BOM_ID.eq(B_ID))
			.join(ITEM.as("i")).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(itemCodeCond)
			.and(itemNameCond)
			.and(versionCond)
			.and(activeCond)
			.orderBy(I_CODE, B_VERSION.desc())
			.fetch()
			.map(RoutingResponse::fromRecord);
	}

	/**
	 * 단건 라우팅 상세 조회. 헤더 + 공정 단계를 함께 반환한다.
	 */
	public Optional<RoutingResponse> findByIdWithSteps(Long id) {
		Record headerRecord = dsl
			.select(
				R_ID.as("id"),
				R_BOM_ID.as("bom_id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("bom_version"),
				R_ACTIVE_YN.as("active_yn"),
				DSL.val(0).as("step_count")
			)
			.from(ROUTING.as("r"))
			.join(BOM.as("b")).on(R_BOM_ID.eq(B_ID))
			.join(ITEM.as("i")).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(R_ID.eq(id))
			.fetchOne();

		if (headerRecord == null) {
			return Optional.empty();
		}

		List<RoutingStepResponse> steps = dsl
			.select(
				RS_ID.as("id"),
				RS_PROCESS_ID.as("process_id"),
				DSL.field(DSL.name("p", "code"), SQLDataType.VARCHAR).as("process_code"),
				DSL.field(DSL.name("p", "name"), SQLDataType.VARCHAR).as("process_name"),
				RS_STEP_ORDER.as("step_order"),
				RS_STANDARD_TIME.as("standard_time"),
				RS_REMARKS.as("remarks")
			)
			.from(ROUTING_STEP.as("rs"))
			.join(PROCESS.as("p")).on(RS_PROCESS_ID.eq(DSL.field(DSL.name("p", "id"), SQLDataType.BIGINT)))
			.where(RS_ROUTING_ID.eq(id))
			.orderBy(RS_STEP_ORDER)
			.fetch()
			.map(RoutingStepResponse::fromRecord);

		return Optional.of(RoutingResponse.fromRecord(headerRecord, steps));
	}

	/**
	 * 특정 품목의 라우팅 목록 조회 — 버전 이력 확인용.
	 */
	public List<RoutingResponse> findByItemId(Long itemId) {
		Field<Integer> stepCount = DSL.field(
			DSL.select(DSL.count())
				.from(ROUTING_STEP.as("rs_count"))
				.where(DSL.field(DSL.name("rs_count", "routing_id"), SQLDataType.BIGINT).eq(R_ID))
		).as("step_count");

		return dsl
			.select(
				R_ID.as("id"),
				R_BOM_ID.as("bom_id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("bom_version"),
				R_ACTIVE_YN.as("active_yn"),
				stepCount
			)
			.from(ROUTING.as("r"))
			.join(BOM.as("b")).on(R_BOM_ID.eq(B_ID))
			.join(ITEM.as("i")).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(B_ITEM_ID.eq(itemId))
			.orderBy(B_VERSION.desc())
			.fetch()
			.map(RoutingResponse::fromRecord);
	}

	/**
	 * BOM ID로 라우팅 단건 조회.
	 */
	public Optional<RoutingResponse> findByBomId(Long bomId) {
		Record record = dsl
			.select(
				R_ID.as("id"),
				R_BOM_ID.as("bom_id"),
				B_ITEM_ID.as("item_id"),
				I_CODE.as("item_code"),
				I_NAME.as("item_name"),
				B_VERSION.as("bom_version"),
				R_ACTIVE_YN.as("active_yn"),
				DSL.val(0).as("step_count")
			)
			.from(ROUTING.as("r"))
			.join(BOM.as("b")).on(R_BOM_ID.eq(B_ID))
			.join(ITEM.as("i")).on(B_ITEM_ID.eq(DSL.field(DSL.name("i", "id"), SQLDataType.BIGINT)))
			.where(R_BOM_ID.eq(bomId))
			.fetchOne();

		return Optional.ofNullable(record).map(RoutingResponse::fromRecord);
	}
}
