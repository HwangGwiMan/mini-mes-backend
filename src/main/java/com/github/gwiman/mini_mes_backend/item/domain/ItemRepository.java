package com.github.gwiman.mini_mes_backend.item.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

	@Query("SELECT i FROM Item i WHERE "
		+ "(:code IS NULL OR i.code LIKE %:code% ESCAPE '\\') AND "
		+ "(:name IS NULL OR i.name LIKE %:name% ESCAPE '\\')")
	List<Item> search(@Param("code") String code, @Param("name") String name);
}
