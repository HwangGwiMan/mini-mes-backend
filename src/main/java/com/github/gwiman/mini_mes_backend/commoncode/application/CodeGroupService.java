package com.github.gwiman.mini_mes_backend.commoncode.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CodeGroupRequest;
import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CodeGroupResponse;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroup;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CodeGroupRepository;
import com.github.gwiman.mini_mes_backend.commoncode.domain.CommonCode;
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
	public void initDefaultCodes() {
		createCodeGroupIfAbsent("TRADE_TYPE",   "거래구분", 1);
		createCommonCodeIfAbsent("TRADE_TYPE", "TRADE_TYPE_01", "매입처",      1);
		createCommonCodeIfAbsent("TRADE_TYPE", "TRADE_TYPE_02", "매출처",      2);
		createCommonCodeIfAbsent("TRADE_TYPE", "TRADE_TYPE_03", "매입/매출처", 3);

		createCodeGroupIfAbsent("PROCESS_TYPE", "공정유형", 2);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_01", "가공", 1);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_02", "조립", 2);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_03", "검사", 3);
		createCommonCodeIfAbsent("PROCESS_TYPE", "PROCESS_TYPE_04", "포장", 4);

		createCodeGroupIfAbsent("DEPT", "부서", 3);
		createCommonCodeIfAbsent("DEPT", "DEPT_01", "생산1부", 1);
		createCommonCodeIfAbsent("DEPT", "DEPT_02", "생산2부", 2);
		createCommonCodeIfAbsent("DEPT", "DEPT_03", "품질부", 3);
		createCommonCodeIfAbsent("DEPT", "DEPT_04", "관리부", 4);

		createCodeGroupIfAbsent("POSITION", "직급", 4);
		createCommonCodeIfAbsent("POSITION", "POSITION_01", "사원", 1);
		createCommonCodeIfAbsent("POSITION", "POSITION_02", "대리", 2);
		createCommonCodeIfAbsent("POSITION", "POSITION_03", "과장", 3);
		createCommonCodeIfAbsent("POSITION", "POSITION_04", "부장", 4);

		createCodeGroupIfAbsent("ITEM_TYPE", "품목유형", 5);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_01", "원자재", 1);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_02", "반제품", 2);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_03", "완제품", 3);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_04", "부자재", 4);
		createCommonCodeIfAbsent("ITEM_TYPE", "ITEM_TYPE_05", "상품", 5);

		createCodeGroupIfAbsent("UNIT", "단위", 6);
		createCommonCodeIfAbsent("UNIT", "UNIT_01", "EA", 1);
		createCommonCodeIfAbsent("UNIT", "UNIT_02", "KG", 2);
		createCommonCodeIfAbsent("UNIT", "UNIT_03", "M", 3);
		createCommonCodeIfAbsent("UNIT", "UNIT_04", "L", 4);
		createCommonCodeIfAbsent("UNIT", "UNIT_05", "BOX", 5);
		createCommonCodeIfAbsent("UNIT", "UNIT_06", "PCS", 6);

		createCodeGroupIfAbsent("QUOTE_STATUS", "견적상태", 7);
		createCommonCodeIfAbsent("QUOTE_STATUS", "QUOTE_STATUS_01", "작성중", 1);
		createCommonCodeIfAbsent("QUOTE_STATUS", "QUOTE_STATUS_02", "제출", 2);
		createCommonCodeIfAbsent("QUOTE_STATUS", "QUOTE_STATUS_03", "승인", 3);
		createCommonCodeIfAbsent("QUOTE_STATUS", "QUOTE_STATUS_04", "거절", 4);
		createCommonCodeIfAbsent("QUOTE_STATUS", "QUOTE_STATUS_05", "수주전환", 5);

		createCodeGroupIfAbsent("ORDER_STATUS", "수주상태", 8);
		createCommonCodeIfAbsent("ORDER_STATUS", "ORDER_STATUS_01", "작성중", 1);
		createCommonCodeIfAbsent("ORDER_STATUS", "ORDER_STATUS_02", "확정", 2);
		createCommonCodeIfAbsent("ORDER_STATUS", "ORDER_STATUS_03", "진행중", 3);
		createCommonCodeIfAbsent("ORDER_STATUS", "ORDER_STATUS_04", "완료", 4);
		createCommonCodeIfAbsent("ORDER_STATUS", "ORDER_STATUS_05", "취소", 5);
	}

	private void createCodeGroupIfAbsent(String groupCode, String groupName, int sortOrder) {
		if (!codeGroupRepository.existsByGroupCode(groupCode)) {
			codeGroupRepository.save(new CodeGroup(groupCode, groupName, sortOrder));
		}
	}

	private void createCommonCodeIfAbsent(String codeGroup, String code, String name, int sortOrder) {
		boolean exists = commonCodeRepository.findByCodeGroupAndUseYnTrueOrderBySortOrder(codeGroup)
			.stream().anyMatch(c -> c.getCode().equals(code));
		if (!exists) {
			commonCodeRepository.save(new CommonCode(codeGroup, code, name, sortOrder));
		}
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
