package com.coderoute.dto.progress;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;

public record UserTopicProgressResponse(
		UUID id,
		UUID userId,
		UUID topicId,
		Integer problemsSolved,
		Integer problemsAttempted,
		BigDecimal accuracy,
		BigDecimal averageTimeSeconds,
		Difficulty currentDifficulty,
		Instant updatedAt) {
}