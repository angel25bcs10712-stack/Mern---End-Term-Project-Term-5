package com.coderoute.dto.admin;

public record AdminStatsResponse(long users, long topics, long problems, long attempts, long solvedAttempts) {
}