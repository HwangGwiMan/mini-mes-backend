package com.github.gwiman.mini_mes_backend.commoncode.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CodeGroupRequest;
import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CodeGroupResponse;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroup;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroupRepository;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeGroupService {

	private final CodeGroupRepository codeGroupRepository;
	private final CommonCodeRepository commonCodeRepository;

	public List<CodeGroupResponse> findAll() {
		return codeGroupRepository.findAllByOrderBySortOrderAsc().stream()
			.map(CodeGroupResponse::from)
			.toList();
	}

	@Transactional
	public CodeGroupResponse create(CodeGroupRequest request) {
		if (codeGroupRepository.existsByGroupCode(request.getGroupCode())) {
			throw new IllegalArgumentException("이미 사용 중인 그룹코드입니다: " + request.getGroupCode());
		}
		return CodeGroupResponse.from(
			codeGroupRepository.save(
				new CodeGroup(request.getGroupCode(), request.getGroupName(), request.getSortOrder())
			)
		);
	}

	@Transactional
	public CodeGroupResponse update(Long id, CodeGroupRequest request) {
		CodeGroup entity = codeGroupRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("그룹코드를 찾을 수 없습니다: " + id));
		if (codeGroupRepository.existsByGroupCodeAndIdNot(request.getGroupCode(), id)) {
			throw new IllegalArgumentException("이미 사용 중인 그룹코드입니다: " + request.getGroupCode());
		}
		entity.update(request.getGroupCode(), request.getGroupName(), request.getSortOrder());
		return CodeGroupResponse.from(entity);
	}

	@Transactional
	public void delete(Long id) {
		CodeGroup entity = codeGroupRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("그룹코드를 찾을 수 없습니다: " + id));
		if (commonCodeRepository.existsByCodeGroup(entity.getGroupCode())) {
			throw new IllegalArgumentException("하위 코드가 존재하는 그룹은 삭제할 수 없습니다. 코드를 먼저 삭제해 주세요.");
		}
		codeGroupRepository.deleteById(id);
	}
}
