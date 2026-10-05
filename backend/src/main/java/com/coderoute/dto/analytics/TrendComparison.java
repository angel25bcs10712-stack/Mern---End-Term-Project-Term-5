package com.coderoute.dto.analytics;

public record TrendComparison(
		Double previousAccuracy,
		Double recentAccuracy,
		Double changePercentagePoints,
		String direction,
		int previousAttempts,
		int recentAttempts) {
}