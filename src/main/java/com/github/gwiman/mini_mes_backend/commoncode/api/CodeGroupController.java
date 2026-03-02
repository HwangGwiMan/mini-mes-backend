package com.github.gwiman.mini_mes_backend.commoncode.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CodeGroupRequest;
import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CodeGroupResponse;
import com.github.gwiman.mini_mes_backend.commoncode.application.CodeGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/code-groups")
@RequiredArgsConstructor
public class CodeGroupController {

	private final CodeGroupService codeGroupService;

	@GetMapping
	public List<CodeGroupResponse> getAll() {
		return codeGroupService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CodeGroupResponse create(@RequestBody @Valid CodeGroupRequest request) {
		return codeGroupService.create(request);
	}

	@PutMapping("/{id}")
	public CodeGroupResponse update(
		@PathVariable Long id,
		@RequestBody @Valid CodeGroupRequest request
	) {
		return codeGroupService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		codeGroupService.delete(id);
	}
}
