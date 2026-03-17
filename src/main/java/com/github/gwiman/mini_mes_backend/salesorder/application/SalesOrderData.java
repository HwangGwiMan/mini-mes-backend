package com.github.gwiman.mini_mes_backend.salesorder.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * 타 모듈(shipment)에서 수주 정보를 조회할 때 사용하는 애플리케이션 레이어 전용 타입.
 * salesorder.domain 엔티티 직접 노출을 차단하기 위해 도입.
 */
public record SalesOrderData(
	Long id,
	Long partnerId,
	Long employeeId,
	List<Line> lines
) {
	public record Line(
		Long id,
		Long itemId,
		BigDecimal quantity,
		BigDecimal unitPrice,
		String remarks
	) {}
}
