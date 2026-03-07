package com.github.gwiman.mini_mes_backend.quote.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

import com.github.gwiman.mini_mes_backend.quote.api.dto.ApprovalRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.ApprovalResponse;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteRequest;
import com.github.gwiman.mini_mes_backend.quote.api.dto.QuoteResponse;
import com.github.gwiman.mini_mes_backend.quote.application.QuoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

	private final QuoteService quoteService;

	@GetMapping
	public List<QuoteResponse> getAll(
		@RequestParam(required = false) String quoteNumber,
		@RequestParam(required = false) Long partnerId,
		@RequestParam(required = false) String statusCode,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return quoteService.findAll(quoteNumber, partnerId, statusCode, fromDate, toDate);
	}

	@GetMapping("/{id}")
	public QuoteResponse getById(@PathVariable Long id) {
		return quoteService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public QuoteResponse create(@RequestBody @Valid QuoteRequest request) {
		return quoteService.create(request);
	}

	@PutMapping("/{id}")
	public QuoteResponse update(@PathVariable Long id, @RequestBody @Valid QuoteRequest request) {
		return quoteService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		quoteService.delete(id);
	}

	@PatchMapping("/{id}/submit")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void submit(@PathVariable Long id, Authentication authentication) {
		quoteService.submit(id, authentication.getName());
	}

	@PostMapping("/{id}/approve")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approve(@PathVariable Long id, @RequestBody @Valid ApprovalRequest request,
		Authentication authentication) {
		quoteService.approve(id, authentication.getName(), request);
	}

	@PostMapping("/{id}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reject(@PathVariable Long id, @RequestBody @Valid ApprovalRequest request,
		Authentication authentication) {
		quoteService.reject(id, authentication.getName(), request);
	}

	@GetMapping("/{id}/approvals")
	public List<ApprovalResponse> getApprovals(@PathVariable Long id) {
		return quoteService.getApprovalHistory(id);
	}
}
