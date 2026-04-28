package com.github.gwiman.mini_mes_backend.materialissue.api.dto;

import java.time.LocalDate;
import java.util.List;

import org.jooq.Record;
import org.jooq.impl.DSL;

public record MaterialIssueResponse(
		Long id,
		String materialIssueNumber,
		/** useCrudPage 호환용 — materialIssueNumber와 동일 */
		String name,
		Long workOrderId,
		String workOrderNumber,
		Long itemId,
		String itemName,
		String statusCode,
		LocalDate issueDate,
		String remarks,
		List<MaterialIssueLineResponse> lines
) {
	/** search 쿼리 결과 레코드로부터 헤더 정보를 매핑한다 — lines는 빈 리스트로 초기화 */
	public static MaterialIssueResponse fromRecord(Record r) {
		String num = r.get(DSL.field("material_issue.material_issue_number", String.class));
		return new MaterialIssueResponse(
				r.get(DSL.field("material_issue.id", Long.class)),
				num,
				num,
				r.get(DSL.field("material_issue.work_order_id", Long.class)),
				r.get(DSL.field("work_order_number", String.class)),
				r.get(DSL.field("item_id", Long.class)),
				r.get(DSL.field("item_name", String.class)),
				r.get(DSL.field("material_issue.status_code", String.class)),
				r.get(DSL.field("material_issue.issue_date", LocalDate.class)),
				r.get(DSL.field("material_issue.remarks", String.class)),
				List.of()
		);
	}

	/** findById 쿼리 결과 레코드로부터 라인 목록을 포함한 전체 응답을 생성한다 */
	public static MaterialIssueResponse fromRecord(Record r, List<MaterialIssueLineResponse> lines) {
		String num = r.get(DSL.field("material_issue.material_issue_number", String.class));
		return new MaterialIssueResponse(
				r.get(DSL.field("material_issue.id", Long.class)),
				num,
				num,
				r.get(DSL.field("material_issue.work_order_id", Long.class)),
				r.get(DSL.field("work_order_number", String.class)),
				r.get(DSL.field("item_id", Long.class)),
				r.get(DSL.field("item_name", String.class)),
				r.get(DSL.field("material_issue.status_code", String.class)),
				r.get(DSL.field("material_issue.issue_date", LocalDate.class)),
				r.get(DSL.field("material_issue.remarks", String.class)),
				lines
		);
	}
}
