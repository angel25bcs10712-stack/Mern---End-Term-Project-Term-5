package com.coderoute.recommendation;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.recommendation.RecommendationResponse;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
	private final RecommendationService recommendationService;

	public RecommendationController(RecommendationService recommendationService) {
		this.recommendationService = recommendationService;
	}

	@GetMapping
	public List<RecommendationResponse> getRecommendations(@AuthenticationPrincipal AuthenticatedUser user) {
		return recommendationService.recommend(user);
	}
}