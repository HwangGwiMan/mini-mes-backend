package com.github.gwiman.mini_mes_backend.process.infrastructure;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Process;
import com.github.gwiman.mini_mes_backend.process.api.dto.ProcessResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProcessQueryRepository {

	private final DSLContext dsl;

	public Optional<ProcessResponse> findById(Long id) {
		Process p = Process.PROCESS;
		return dsl
			.selectFrom(p)
			.where(p.ID.eq(id))
			.fetchOptional()
			.map(ProcessResponse::fromRecord);
	}
}
