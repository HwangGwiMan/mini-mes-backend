package com.github.gwiman.mini_mes_backend.employee.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
	@NotBlank(message = "사번은 필수입니다.")
	@Size(max = 50, message = "사번은 50자 이하여야 합니다.")
	String code,

	@NotBlank(message = "성명은 필수입니다.")
	@Size(max = 100, message = "성명은 100자 이하여야 합니다.")
	String name,

	@Size(max = 20, message = "부서 코드는 20자 이하여야 합니다.")
	String deptCode,

	@Size(max = 20, message = "직급 코드는 20자 이하여야 합니다.")
	String positionCode,

	LocalDate hireDate,

	@Size(max = 20, message = "연락처는 20자 이하여야 합니다.")
	String phone,

	@Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
	String email,

	Boolean useYn,

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	int sortOrder
) {
	public EmployeeRequest {
		// useYn 미전달 시 기본값 true
		if (useYn == null) useYn = true;
	}
}
