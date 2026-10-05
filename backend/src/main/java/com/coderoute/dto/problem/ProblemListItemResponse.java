package com.coderoute.dto.problem;

import java.util.List;
import java.util.UUID;

import com.coderoute.entity.Problem;
import com.coderoute.entity.enums.Difficulty;

public record ProblemListItemResponse(
		UUID id,
		String title,
		String description,
		Difficulty difficulty,
		UUID topicId,
		String topicName,
		List<String> tags,
		Integer estimatedTimeMinutes,
		boolean solved) {
	public static ProblemListItemResponse from(Problem problem, boolean solved) {
		return new ProblemListItemResponse(problem.getId(), problem.getTitle(), problem.getDescription(),
				problem.getDifficulty(), problem.getTopic().getId(), problem.getTopic().getName(),
				List.of(problem.getTags()), problem.getEstimatedTimeMinutes(), solved);
	}
}