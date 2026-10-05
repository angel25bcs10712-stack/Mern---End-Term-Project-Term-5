package com.coderoute.dto.admin;

import java.util.List;
import java.util.UUID;

import com.coderoute.entity.Problem;
import com.coderoute.entity.enums.Difficulty;

public record AdminProblemResponse(
		UUID id,
		String title,
		String description,
		Difficulty difficulty,
		UUID topicId,
		String topicName,
		String externalUrl,
		List<String> tags,
		Integer estimatedTimeMinutes) {
	public static AdminProblemResponse from(Problem problem) {
		return new AdminProblemResponse(problem.getId(), problem.getTitle(), problem.getDescription(),
				problem.getDifficulty(), problem.getTopic().getId(), problem.getTopic().getName(),
				problem.getExternalUrl(), List.of(problem.getTags()), problem.getEstimatedTimeMinutes());
	}
}