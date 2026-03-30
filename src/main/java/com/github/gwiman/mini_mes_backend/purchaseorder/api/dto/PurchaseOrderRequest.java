package com.github.gwiman.mini_mes_backend.purchaseorder.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchaseOrderRequest(
	@NotNull LocalDate orderDate,
	@NotNull Long partnerId,
	LocalDate expectedArrivalDate,
	@Size(max = 200) String remarks,
	@NotEmpty @Valid List<PurchaseOrderLineRequest> lines
) {}
