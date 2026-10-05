package com.coderoute.dto.problem;

import java.util.List;
import java.util.UUID;

import com.coderoute.entity.Problem;
import com.coderoute.entity.enums.Difficulty;

public record ProblemDetailResponse(
		UUID id,
		String title,
		String description,
		Difficulty difficulty,
		UUID topicId,
		String topicName,
		String externalUrl,
		List<String> tags,
		Integer estimatedTimeMinutes,
		boolean solved) {
	public static ProblemDetailResponse from(Problem problem, boolean solved) {
		return new ProblemDetailResponse(problem.getId(), problem.getTitle(), problem.getDescription(),
				problem.getDifficulty(), problem.getTopic().getId(), problem.getTopic().getName(),
				problem.getExternalUrl(), List.of(problem.getTags()), problem.getEstimatedTimeMinutes(), solved);
	}
}