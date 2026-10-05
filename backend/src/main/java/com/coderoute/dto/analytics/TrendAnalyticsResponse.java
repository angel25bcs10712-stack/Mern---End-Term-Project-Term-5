package com.coderoute.dto.analytics;

import java.util.List;

public record TrendAnalyticsResponse(TrendComparison improvementTrend, List<WeeklyTrend> weeklyTrend) {
}