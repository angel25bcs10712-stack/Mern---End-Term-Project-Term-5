package com.coderoute.dto.goal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.coderoute.entity.enums.GoalStatus;

public record LearningGoalResponse(
		UUID id,
		UUID userId,
		Integer target,
		LocalDate targetDate,
		BigDecimal weeklyHours,
		GoalStatus status,
		Instant createdAt,
		Instant updatedAt) {
}