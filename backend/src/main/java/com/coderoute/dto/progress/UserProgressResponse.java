package com.coderoute.dto.progress;

import java.util.List;

public record UserProgressResponse(
		long totalProblems,
		long problemsSolved,
		long problemsAttempted,
		List<TopicProgressResponse> topics) {
}