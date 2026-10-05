package com.coderoute.dto.attempt;

import java.time.Instant;
import java.util.UUID;

import com.coderoute.entity.enums.AttemptStatus;

public record ProblemAttemptResponse(
		UUID id,
		UUID userId,
		UUID problemId,
		AttemptStatus status,
		Integer timeTakenSeconds,
		Integer attempts,
		Instant solvedAt,
		Instant createdAt,
		Instant updatedAt) {
}