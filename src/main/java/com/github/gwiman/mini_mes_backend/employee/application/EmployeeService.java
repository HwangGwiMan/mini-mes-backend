package com.github.gwiman.mini_mes_backend.employee.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.auth.domain.Role;
import com.github.gwiman.mini_mes_backend.auth.domain.User;
import com.github.gwiman.mini_mes_backend.auth.domain.UserRepository;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeRequest;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeResponse;
import com.github.gwiman.mini_mes_backend.employee.domain.Employee;
import com.github.gwiman.mini_mes_backend.employee.domain.EmployeeRepository;
import com.github.gwiman.mini_mes_backend.employee.internal.EmployeeQueryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

	private static final String DEFAULT_PASSWORD = "pw1234";

	private final EmployeeRepository employeeRepository;
	private final EmployeeQueryRepository employeeQueryRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public List<EmployeeResponse> findAll(String code, String name, String deptCode) {
		return employeeRepository.search(QueryParamEscaper.escapeLike(code), QueryParamEscaper.escapeLike(name), deptCode).stream()
			.map(EmployeeResponse::from)
			.toList();
	}

	public EmployeeResponse findById(Long id) {
		return employeeQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("사원을 찾을 수 없습니다: " + id));
	}

	@Transactional
	public EmployeeResponse create(EmployeeRequest request) {
		if (employeeRepository.existsByCode(request.getCode())) {
			throw new BusinessRuleViolationException("이미 사용 중인 사번입니다: " + request.getCode());
		}
		if (userRepository.existsByUsername(request.getCode())) {
			throw new BusinessRuleViolationException("해당 사번으로 이미 로그인 계정이 존재합니다: " + request.getCode());
		}
		Employee entity = new Employee(
			request.getCode(),
			request.getName(),
			request.getDeptCode(),
			request.getPositionCode(),
			request.getHireDate(),
			request.getPhone(),
			request.getEmail(),
			request.isUseYn(),
			request.getSortOrder()
		);
		entity = employeeRepository.save(entity);

		User user = new User(
			entity.getCode(),
			passwordEncoder.encode(DEFAULT_PASSWORD),
			Role.ROLE_USER,
			entity.getId()
		);
		userRepository.save(user);

		return EmployeeResponse.from(entity);
	}

	@Transactional
	public EmployeeResponse update(Long id, EmployeeRequest request) {
		Employee entity = employeeRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("사원을 찾을 수 없습니다: " + id));
		if (employeeRepository.existsByCodeAndIdNot(request.getCode(), id)) {
			throw new BusinessRuleViolationException("이미 사용 중인 사번입니다: " + request.getCode());
		}

		String oldCode = entity.getCode();
		String newCode = request.getCode();
		if (!oldCode.equals(newCode)) {
			if (userRepository.existsByUsername(newCode)) {
				throw new BusinessRuleViolationException("해당 사번으로 이미 로그인 계정이 존재합니다: " + newCode);
			}
			Optional<User> userOpt = userRepository.findByEmployeeId(id);
			userOpt.ifPresent(user -> user.updateUsername(newCode));
		}

		entity.update(
			request.getCode(),
			request.getName(),
			request.getDeptCode(),
			request.getPositionCode(),
			request.getHireDate(),
			request.getPhone(),
			request.getEmail(),
			request.isUseYn(),
			request.getSortOrder()
		);
		return EmployeeResponse.from(entity);
	}

	public boolean exists(Long id) {
		return employeeRepository.existsById(id);
	}

	@Transactional
	public void delete(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResourceNotFoundException("사원을 찾을 수 없습니다: " + id);
		}
		userRepository.findByEmployeeId(id).ifPresent(userRepository::delete);
		employeeRepository.deleteById(id);
	}

	@Transactional
	public void initDefaultEmployees() {
		// 사원 등록 시 사번과 동일한 로그인 계정도 함께 생성됨 (초기 비밀번호: pw1234)
		createEmployeeIfAbsent("E001", "김영업", "DEPT_04", "POSITION_03", "010-1111-2222", "kim@company.com", 1);
		createEmployeeIfAbsent("E002", "이담당", "DEPT_01", "POSITION_02", "010-2222-3333", "lee@company.com", 2);
		createEmployeeIfAbsent("E003", "박생산", "DEPT_01", "POSITION_01", "010-3333-4444", "park@company.com", 3);
		createEmployeeIfAbsent("E004", "최품질", "DEPT_03", "POSITION_02", "010-4444-5555", "choi@company.com", 4);
		createEmployeeIfAbsent("E005", "정관리", "DEPT_04", "POSITION_04", "010-5555-6666", "jung@company.com", 5);
	}

	private void createEmployeeIfAbsent(String code, String name, String deptCode,
		String positionCode, String phone, String email, int sortOrder) {
		if (employeeRepository.existsByCode(code)) return;

		Employee entity = employeeRepository.save(
			new Employee(code, name, deptCode, positionCode,
				java.time.LocalDate.of(2020, 1, 1), phone, email, true, sortOrder)
		);
		if (!userRepository.existsByUsername(code)) {
			userRepository.save(new User(code, passwordEncoder.encode(DEFAULT_PASSWORD), Role.ROLE_USER, entity.getId()));
		}
	}

}
