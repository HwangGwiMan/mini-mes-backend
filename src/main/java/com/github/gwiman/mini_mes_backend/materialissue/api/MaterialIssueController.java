package com.github.gwiman.mini_mes_backend.materialissue.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueRequest;
import com.github.gwiman.mini_mes_backend.materialissue.api.dto.MaterialIssueResponse;
import com.github.gwiman.mini_mes_backend.materialissue.application.MaterialIssueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 자재 출고 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/material-issues")
@RequiredArgsConstructor
public class MaterialIssueController {

	private final MaterialIssueService materialIssueService;

	@GetMapping
	public List<MaterialIssueResponse> getAll(
			@RequestParam(required = false) String materialIssueNumber,
			@RequestParam(required = false) String workOrderNumber,
			@RequestParam(required = false) String statusCode) {
		return materialIssueService.findAll(materialIssueNumber, workOrderNumber, statusCode);
	}

	@GetMapping("/{id}")
	public MaterialIssueResponse getById(@PathVariable Long id) {
		return materialIssueService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MaterialIssueResponse create(@RequestBody @Valid MaterialIssueRequest request) {
		return materialIssueService.create(request);
	}

	@PutMapping("/{id}")
	public MaterialIssueResponse update(
			@PathVariable Long id,
			@RequestBody @Valid MaterialIssueRequest request) {
		return materialIssueService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		materialIssueService.delete(id);
	}

	@PatchMapping("/{id}/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirm(@PathVariable Long id) {
		materialIssueService.confirm(id);
	}

	@PatchMapping("/{id}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable Long id) {
		materialIssueService.cancel(id);
	}
}
