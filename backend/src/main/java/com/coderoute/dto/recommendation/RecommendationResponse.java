package com.coderoute.dto.recommendation;

import java.math.BigDecimal;

import com.coderoute.dto.problem.ProblemListItemResponse;
import com.coderoute.entity.enums.Difficulty;

public record RecommendationResponse(
		ProblemListItemResponse problem,
		String reasonForRecommendation,
		BigDecimal recommendationScore,
		String topic,
		Difficulty difficulty) {
}