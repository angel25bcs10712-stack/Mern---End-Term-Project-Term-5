package com.coderoute.dto.analytics;

import com.coderoute.entity.enums.Difficulty;

public record DifficultyPerformance(
		Difficulty difficulty,
		int attemptCount,
		int solvedCount,
		double accuracy,
		Double averageSolvingTimeSeconds) {
}