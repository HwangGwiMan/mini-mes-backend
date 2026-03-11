package com.github.gwiman.mini_mes_backend.process.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
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
		return processRepository.search(escapeLike(code), escapeLike(name)).stream()
			.map(ProcessResponse::from)
			.toList();
	}

	public ProcessResponse findById(Long id) {
		return processQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("공정을 찾을 수 없습니다: " + id));
	}

	@Transactional
	public ProcessResponse create(ProcessRequest request) {
		if (processRepository.existsByCode(request.getCode())) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.getCode());
		}
		Process entity = new Process(
			request.getCode(),
			request.getName(),
			request.getProcessTypeCode(),
			request.getStandardTime(),
			request.getDescription(),
			request.getSortOrder()
		);
		return ProcessResponse.from(processRepository.save(entity));
	}

	@Transactional
	public ProcessResponse update(Long id, ProcessRequest request) {
		Process entity = processRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("공정을 찾을 수 없습니다: " + id));
		if (processRepository.existsByCodeAndIdNot(request.getCode(), id)) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.getCode());
		}
		entity.update(
			request.getCode(),
			request.getName(),
			request.getProcessTypeCode(),
			request.getStandardTime(),
			request.getDescription(),
			request.getSortOrder()
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

	private String escapeLike(String value) {
		if (value == null || value.isBlank()) return null;
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
