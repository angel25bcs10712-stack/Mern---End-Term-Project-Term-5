package com.coderoute.analytics;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.analytics.PerformanceAnalyticsResponse;
import com.coderoute.dto.analytics.TrendAnalyticsResponse;
import com.coderoute.dto.analytics.WeakTopic;

@RestController
@RequestMapping("/api/analytics/me")
public class AnalyticsController {
	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/performance")
	public PerformanceAnalyticsResponse performance(@AuthenticationPrincipal AuthenticatedUser user) {
		return analyticsService.performance(user.getId());
	}

	@GetMapping("/weak-topics")
	public List<WeakTopic> weakTopics(@AuthenticationPrincipal AuthenticatedUser user) {
		return analyticsService.weakTopics(user.getId());
	}

	@GetMapping("/trend")
	public TrendAnalyticsResponse trend(@AuthenticationPrincipal AuthenticatedUser user) {
		return analyticsService.trend(user.getId());
	}
}