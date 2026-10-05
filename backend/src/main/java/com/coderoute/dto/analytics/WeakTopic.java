package com.coderoute.dto.analytics;

public record WeakTopic(String topic, int attemptCount, int solvedCount, double accuracy, String reason) {
}