package com.github.gwiman.mini_mes_backend.bom.api.dto;

import java.time.LocalDate;
import java.util.List;

import org.jooq.Record;

public record BomResponse(
	Long id,
	Long itemId,
	String itemCode,
	String itemName,
	/** useCrudPage 호환용 — itemName과 동일 */
	String name,
	String version,
	LocalDate validFrom,
	LocalDate validTo,
	Boolean activeYn,
	int lineCount,
	List<BomLineResponse> lines
) {
	/** 목록 조회 결과 매핑 (lines 없음) */
	public static BomResponse fromRecord(Record r) {
		return new BomResponse(
			r.get("id", Long.class),
			r.get("item_id", Long.class),
			r.get("item_code", String.class),
			r.get("item_name", String.class),
			r.get("item_name", String.class),
			r.get("version", String.class),
			r.get("valid_from", LocalDate.class),
			r.get("valid_to", LocalDate.class),
			r.get("active_yn", Boolean.class),
			r.get("line_count", Integer.class),
			List.of()
		);
	}

	/** 상세 조회 결과 매핑 (lines 포함) */
	public static BomResponse fromRecord(Record r, List<BomLineResponse> lines) {
		BomResponse base = fromRecord(r);
		return new BomResponse(
			base.id(), base.itemId(), base.itemCode(), base.itemName(), base.name(),
			base.version(), base.validFrom(), base.validTo(), base.activeYn(),
			lines.size(), lines
		);
	}
}
