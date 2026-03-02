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
			.orElseThrow(() -> new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + id));
		return CommonCodeResponse.from(entity);
	}

	public List<CommonCodeResponse> findAll(String codeGroup, String code, String name) {
		String groupParam = (codeGroup != null && !codeGroup.isBlank()) ? codeGroup : null;
		return commonCodeRepository.search(groupParam, toLikeParam(code), toLikeParam(name)).stream()
			.map(CommonCodeResponse::from)
			.toList();
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

	@Transactional
	public CommonCodeResponse update(Long id, CommonCodeRequest request) {
		CommonCode entity = commonCodeRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + id));
		entity.update(
			request.getCodeGroup(),
			request.getCode(),
			request.getName(),
			request.getSortOrder()
		);
		return CommonCodeResponse.from(entity);
	}

	@Transactional
	public void delete(Long id) {
		if (!commonCodeRepository.existsById(id)) {
			throw new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + id);
		}
		commonCodeRepository.deleteById(id);
	}

	private String toLikeParam(String value) {
		return (value == null || value.isBlank()) ? null : value;
	}
}
