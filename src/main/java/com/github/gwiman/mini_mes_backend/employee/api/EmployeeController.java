package com.github.gwiman.mini_mes_backend.employee.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeRequest;
import com.github.gwiman.mini_mes_backend.employee.api.dto.EmployeeResponse;
import com.github.gwiman.mini_mes_backend.employee.application.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

	private final EmployeeService employeeService;

	@GetMapping
	public List<EmployeeResponse> getAll(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String name,
		@RequestParam(required = false) String deptCode) {
		return employeeService.findAll(code, name, deptCode);
	}

	@GetMapping("/{id}")
	public EmployeeResponse getById(@PathVariable Long id) {
		return employeeService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EmployeeResponse create(@RequestBody @Valid EmployeeRequest request) {
		return employeeService.create(request);
	}

	@PutMapping("/{id}")
	public EmployeeResponse update(@PathVariable Long id, @RequestBody @Valid EmployeeRequest request) {
		return employeeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		employeeService.delete(id);
	}
}
