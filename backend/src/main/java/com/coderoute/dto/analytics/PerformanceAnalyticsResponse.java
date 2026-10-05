package com.coderoute.dto.analytics;

import java.util.List;

public record PerformanceAnalyticsResponse(
		int totalAttempts,
		int solvedAttempts,
		double overallAccuracy,
		Double averageSolvingTimeSeconds,
		double consistencyScore,
		List<TopicAccuracy> topicAccuracy,
		List<DifficultyPerformance> difficultyPerformance,
		List<WeakTopic> weakTopics,
		TrendComparison improvementTrend,
		List<WeeklyTrend> weeklyTrend) {
}