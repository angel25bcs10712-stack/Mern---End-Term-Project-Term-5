package com.coderoute.dto.leetcode;

public record LeetCodeStatsResponse(
		String username,
		Integer totalSolved,
		Integer easy,
		Integer medium,
		Integer hard,
		Double contestRating) {
}