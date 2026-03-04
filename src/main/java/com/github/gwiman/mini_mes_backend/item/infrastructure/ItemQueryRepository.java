package com.github.gwiman.mini_mes_backend.item.infrastructure;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import com.github.gwiman.mini_mes_backend.jooq.tables.Item;
import com.github.gwiman.mini_mes_backend.item.api.dto.ItemResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemQueryRepository {

	private final DSLContext dsl;

	public Optional<ItemResponse> findById(Long id) {
		Item i = Item.ITEM;
		return dsl
			.selectFrom(i)
			.where(i.ID.eq(id))
			.fetchOptional()
			.map(ItemResponse::fromRecord);
	}
}
