package com.coderoute.problem;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.progress.UserProgressResponse;

@RestController
@RequestMapping("/api/users/me/progress")
public class UserProgressController {
	private final ProblemService problemService;

	public UserProgressController(ProblemService problemService) {
		this.problemService = problemService;
	}

	@GetMapping
	public UserProgressResponse getProgress(@AuthenticationPrincipal AuthenticatedUser user) {
		return problemService.progress(user);
	}
}