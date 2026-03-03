package com.github.gwiman.mini_mes_backend.employee.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeRequest {

	@NotBlank(message = "사번은 필수입니다.")
	@Size(max = 50, message = "사번은 50자 이하여야 합니다.")
	private String code;

	@NotBlank(message = "성명은 필수입니다.")
	@Size(max = 100, message = "성명은 100자 이하여야 합니다.")
	private String name;

	@Size(max = 20, message = "부서 코드는 20자 이하여야 합니다.")
	private String deptCode;

	@Size(max = 20, message = "직급 코드는 20자 이하여야 합니다.")
	private String positionCode;

	private LocalDate hireDate;

	@Size(max = 20, message = "연락처는 20자 이하여야 합니다.")
	private String phone;

	@Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
	private String email;

	private boolean useYn = true;

	@Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
	private int sortOrder;
}
