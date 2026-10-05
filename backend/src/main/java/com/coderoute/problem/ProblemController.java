package com.coderoute.problem;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.attempt.ProblemAttemptRequest;
import com.coderoute.dto.attempt.ProblemAttemptResponse;
import com.coderoute.dto.problem.ProblemDetailResponse;
import com.coderoute.dto.problem.ProblemPageResponse;
import com.coderoute.entity.enums.Difficulty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/problems")
@Validated
public class ProblemController {
	private final ProblemService problemService;

	public ProblemController(ProblemService problemService) {
		this.problemService = problemService;
	}

	@GetMapping
	public ProblemPageResponse list(
			@org.springframework.security.core.annotation.AuthenticationPrincipal AuthenticatedUser user,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID topicId,
			@RequestParam(required = false) Difficulty difficulty,
			@RequestParam(required = false) Boolean solved,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
		return problemService.list(user, search, topicId, difficulty, solved, page, size);
	}

	@GetMapping("/{id}")
	public ProblemDetailResponse get(@PathVariable UUID id,
			@org.springframework.security.core.annotation.AuthenticationPrincipal AuthenticatedUser user) {
		return problemService.get(id, user);
	}

	@PostMapping("/{id}/attempt")
	@ResponseStatus(HttpStatus.CREATED)
	public ProblemAttemptResponse attempt(@PathVariable UUID id, @Valid @RequestBody ProblemAttemptRequest request,
			@org.springframework.security.core.annotation.AuthenticationPrincipal AuthenticatedUser user) {
		return problemService.record(id, request, user, false);
	}

	@PostMapping("/{id}/solve")
	@ResponseStatus(HttpStatus.CREATED)
	public ProblemAttemptResponse solve(@PathVariable UUID id, @Valid @RequestBody ProblemAttemptRequest request,
			@org.springframework.security.core.annotation.AuthenticationPrincipal AuthenticatedUser user) {
		return problemService.record(id, request, user, true);
	}
}