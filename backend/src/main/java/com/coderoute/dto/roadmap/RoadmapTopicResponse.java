package com.coderoute.dto.roadmap;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;

public record RoadmapTopicResponse(
		UUID topicId,
		String topicName,
		Difficulty difficulty,
		List<RoadmapPrerequisiteResponse> prerequisites,
		int level,
		int totalProblems,
		int problemsSolved,
		int problemsAttempted,
		BigDecimal accuracy,
		int progressPercent,
		RoadmapTopicStatus status,
		boolean recommended,
		int recommendationRank,
		BigDecimal recommendationScore,
		String nextStepReason) {
}