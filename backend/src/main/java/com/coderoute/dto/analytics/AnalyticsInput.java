package com.coderoute.dto.analytics;

import java.util.List;

public record AnalyticsInput(List<AnalyticsAttemptData> attempts) {
}