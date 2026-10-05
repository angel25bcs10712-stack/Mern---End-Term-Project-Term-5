package com.coderoute.dto.analytics;

import java.time.Instant;

import com.coderoute.entity.enums.AttemptStatus;
import com.coderoute.entity.enums.Difficulty;

public record AnalyticsAttemptData(
		String topic,
		Difficulty difficulty,
		AttemptStatus status,
		Integer attempts,
		Integer timeTakenSeconds,
		Instant occurredAt) {
}