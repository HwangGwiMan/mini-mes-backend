package com.github.gwiman.mini_mes_backend.commoncode.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeRequest;
import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeResponse;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCode;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

	private final CommonCodeRepository commonCodeRepository;

	public CommonCodeResponse findById(Long id) {
		CommonCode entity = commonCodeRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("CommonCode not found: " + id));
		return CommonCodeResponse.from(entity);
	}

	public List<CommonCodeResponse> findByGroup(String groupCode) {
		return commonCodeRepository.findByCodeGroupAndUseYnTrueOrderBySortOrder(groupCode).stream()
			.map(CommonCodeResponse::from)
			.toList();
	}

	@Transactional
	public CommonCodeResponse create(CommonCodeRequest request) {
		CommonCode entity = new CommonCode(
			request.getCodeGroup(),
			request.getCode(),
			request.getName(),
			request.getSortOrder()
		);
		return CommonCodeResponse.from(commonCodeRepository.save(entity));
	}
}
