package com.coderoute.dto.analytics;

public record WeeklyTrend(String weekStart, int attemptCount, int solvedCount, double accuracy) {
}