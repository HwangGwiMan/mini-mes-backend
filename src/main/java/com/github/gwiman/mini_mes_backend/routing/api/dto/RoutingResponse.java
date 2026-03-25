package com.github.gwiman.mini_mes_backend.routing.api.dto;

import java.util.List;

import org.jooq.Record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingResponse {

	private Long id;
	private Long bomId;
	private Long itemId;
	private String itemCode;
	private String itemName;
	/** useCrudPage 호환용 — itemName과 동일 */
	private String name;
	private String bomVersion;
	private Boolean activeYn;
	private int stepCount;
	private List<RoutingStepResponse> steps;

	/** 목록 조회 결과 매핑 (steps 없음) */
	public static RoutingResponse fromRecord(Record r) {
		return new RoutingResponse(
			r.get("id", Long.class),
			r.get("bom_id", Long.class),
			r.get("item_id", Long.class),
			r.get("item_code", String.class),
			r.get("item_name", String.class),
			r.get("item_name", String.class),
			r.get("bom_version", String.class),
			r.get("active_yn", Boolean.class),
			r.get("step_count", Integer.class),
			List.of()
		);
	}

	/** 상세 조회 결과 매핑 (steps 포함) */
	public static RoutingResponse fromRecord(Record r, List<RoutingStepResponse> steps) {
		RoutingResponse base = fromRecord(r);
		return new RoutingResponse(
			base.id, base.bomId, base.itemId, base.itemCode, base.itemName, base.name,
			base.bomVersion, base.activeYn, steps.size(), steps
		);
	}
}
