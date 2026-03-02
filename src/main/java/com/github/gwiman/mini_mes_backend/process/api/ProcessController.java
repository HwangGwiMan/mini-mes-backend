package com.github.gwiman.mini_mes_backend.process.api;

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

import com.github.gwiman.mini_mes_backend.process.api.dto.ProcessRequest;
import com.github.gwiman.mini_mes_backend.process.api.dto.ProcessResponse;
import com.github.gwiman.mini_mes_backend.process.application.ProcessService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
public class ProcessController {

	private final ProcessService processService;

	@GetMapping
	public List<ProcessResponse> getAll(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String name) {
		return processService.findAll(code, name);
	}

	@GetMapping("/{id}")
	public ProcessResponse getById(@PathVariable Long id) {
		return processService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProcessResponse create(@RequestBody @Valid ProcessRequest request) {
		return processService.create(request);
	}

	@PutMapping("/{id}")
	public ProcessResponse update(@PathVariable Long id, @RequestBody @Valid ProcessRequest request) {
		return processService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		processService.delete(id);
	}
}
