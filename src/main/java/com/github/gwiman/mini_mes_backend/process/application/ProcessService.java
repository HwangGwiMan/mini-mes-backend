package com.github.gwiman.mini_mes_backend.process.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.process.api.dto.ProcessRequest;
import com.github.gwiman.mini_mes_backend.process.api.dto.ProcessResponse;
import com.github.gwiman.mini_mes_backend.process.domain.Process;
import com.github.gwiman.mini_mes_backend.process.domain.ProcessRepository;
import com.github.gwiman.mini_mes_backend.process.internal.ProcessQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessService {

	private final ProcessRepository processRepository;
	private final ProcessQueryRepository processQueryRepository;

	public List<ProcessResponse> findAll(String code, String name) {
		return processRepository.search(QueryParamEscaper.escapeLike(code), QueryParamEscaper.escapeLike(name)).stream()
			.map(ProcessResponse::from)
			.toList();
	}

	public ProcessResponse findById(Long id) {
		return processQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("공정을 찾을 수 없습니다: " + id));
	}

	@Transactional
	public ProcessResponse create(ProcessRequest request) {
		if (processRepository.existsByCode(request.code())) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.code());
		}
		Process entity = new Process(
			request.code(),
			request.name(),
			request.processTypeCode(),
			request.standardTime(),
			request.description(),
			request.sortOrder()
		);
		return ProcessResponse.from(processRepository.save(entity));
	}

	@Transactional
	public ProcessResponse update(Long id, ProcessRequest request) {
		Process entity = processRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("공정을 찾을 수 없습니다: " + id));
		if (processRepository.existsByCodeAndIdNot(request.code(), id)) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.code());
		}
		entity.update(
			request.code(),
			request.name(),
			request.processTypeCode(),
			request.standardTime(),
			request.description(),
			request.sortOrder()
		);
		return ProcessResponse.from(entity);
	}

	@Transactional
	public void delete(Long id) {
		if (!processRepository.existsById(id)) {
			throw new ResourceNotFoundException("공정을 찾을 수 없습니다: " + id);
		}
		processRepository.deleteById(id);
	}

	public boolean exists(Long id) {
		return processRepository.existsById(id);
	}

	/** routing 도메인의 공정 일괄 존재 검증용 (N+1 방지) */
	public Set<Long> findExistingIds(Set<Long> ids) {
		return processRepository.findAllById(ids).stream()
			.map(Process::getId)
			.collect(Collectors.toSet());
	}

}
