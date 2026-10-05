package com.coderoute.dto.problem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;

public record ProblemResponse(
		UUID id,
		String title,
		String description,
		Difficulty difficulty,
		UUID topicId,
		String externalUrl,
		List<String> tags,
		Integer estimatedTimeMinutes,
		Instant createdAt,
		Instant updatedAt) {
}