package com.github.gwiman.mini_mes_backend.partner.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartnerRequest {

	@NotBlank(message = "코드는 필수입니다.")
	@Size(max = 50, message = "코드는 50자 이하여야 합니다.")
	private String code;

	@NotBlank(message = "명칭은 필수입니다.")
	@Size(max = 100, message = "명칭은 100자 이하여야 합니다.")
	private String name;

	@Size(max = 20, message = "사업자번호는 20자 이하여야 합니다.")
	@Pattern(
		regexp = "^$|^\\d{3}-\\d{2}-\\d{5}$",
		message = "사업자번호 형식이 올바르지 않습니다. (예: 123-45-67890)"
	)
	private String businessNumber;

	@Size(max = 50, message = "대표자명은 50자 이하여야 합니다.")
	private String ceoName;

	@Size(max = 200, message = "주소는 200자 이하여야 합니다.")
	private String address;

	@Size(max = 20, message = "연락처1은 20자 이하여야 합니다.")
	@Pattern(
		regexp = "^$|^\\d{2,3}-\\d{3,4}-\\d{4}$",
		message = "연락처1 형식이 올바르지 않습니다. (예: 02-1234-5678)"
	)
	private String phone1;

	@Size(max = 20, message = "연락처2는 20자 이하여야 합니다.")
	@Pattern(
		regexp = "^$|^\\d{2,3}-\\d{3,4}-\\d{4}$",
		message = "연락처2 형식이 올바르지 않습니다. (예: 010-1234-5678)"
	)
	private String phone2;

	@Size(max = 20, message = "거래구분 코드는 20자 이하여야 합니다.")
	private String tradeTypeCode;
}
