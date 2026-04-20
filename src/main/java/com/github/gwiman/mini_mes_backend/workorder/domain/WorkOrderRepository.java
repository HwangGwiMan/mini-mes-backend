package com.github.gwiman.mini_mes_backend.workorder.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

	@Query("SELECT wo FROM WorkOrder wo LEFT JOIN FETCH wo.materials WHERE wo.id = :id")
	Optional<WorkOrder> findByIdWithMaterials(@Param("id") Long id);

	/** materials + routings를 한 번에 페치. update/confirm/cancel 시 사용. */
	@Query("SELECT DISTINCT wo FROM WorkOrder wo LEFT JOIN FETCH wo.materials LEFT JOIN FETCH wo.routings WHERE wo.id = :id")
	Optional<WorkOrder> findByIdWithAll(@Param("id") Long id);
}
