package com.github.gwiman.mini_mes_backend.employee.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.auth.domain.Role;
import com.github.gwiman.mini_mes_backend.auth.domain.User;
import com.github.gwiman.mini_mes_backend.auth.domain.UserRepository;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeRequest;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeResponse;
import com.github.gwiman.mini_mes_backend.employee.domain.Employee;
import com.github.gwiman.mini_mes_backend.employee.domain.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

	private static final String DEFAULT_PASSWORD = "pw1234";

	private final EmployeeRepository employeeRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public List<EmployeeResponse> findAll(String code, String name, String deptCode) {
		return employeeRepository.search(escapeLike(code), escapeLike(name), deptCode).stream()
			.map(EmployeeResponse::from)
			.toList();
	}

	public EmployeeResponse findById(Long id) {
		Employee entity = employeeRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다: " + id));
		return EmployeeResponse.from(entity);
	}

	@Transactional
	public EmployeeResponse create(EmployeeRequest request) {
		if (employeeRepository.existsByCode(request.getCode())) {
			throw new IllegalArgumentException("이미 사용 중인 사번입니다: " + request.getCode());
		}
		if (userRepository.existsByUsername(request.getCode())) {
			throw new IllegalArgumentException("해당 사번으로 이미 로그인 계정이 존재합니다: " + request.getCode());
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
			.orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다: " + id));
		if (employeeRepository.existsByCodeAndIdNot(request.getCode(), id)) {
			throw new IllegalArgumentException("이미 사용 중인 사번입니다: " + request.getCode());
		}

		String oldCode = entity.getCode();
		String newCode = request.getCode();
		if (!oldCode.equals(newCode)) {
			if (userRepository.existsByUsername(newCode)) {
				throw new IllegalArgumentException("해당 사번으로 이미 로그인 계정이 존재합니다: " + newCode);
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

	@Transactional
	public void delete(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new IllegalArgumentException("사원을 찾을 수 없습니다: " + id);
		}
		userRepository.findByEmployeeId(id).ifPresent(userRepository::delete);
		employeeRepository.deleteById(id);
	}

	private String escapeLike(String value) {
		if (value == null || value.isBlank()) return null;
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
