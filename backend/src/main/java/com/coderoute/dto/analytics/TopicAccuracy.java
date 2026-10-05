package com.coderoute.dto.analytics;

public record TopicAccuracy(
		String topic,
		int attemptCount,
		int solvedCount,
		double accuracy,
		Double averageSolvingTimeSeconds) {
}