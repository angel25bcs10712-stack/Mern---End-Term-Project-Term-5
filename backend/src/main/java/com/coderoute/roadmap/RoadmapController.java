package com.coderoute.roadmap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.roadmap.LearningRoadmapResponse;

@RestController
@RequestMapping("/api/users/me/roadmap")
public class RoadmapController {
	private final RoadmapService roadmapService;

	public RoadmapController(RoadmapService roadmapService) {
		this.roadmapService = roadmapService;
	}

	@GetMapping
	public LearningRoadmapResponse getRoadmap(@AuthenticationPrincipal AuthenticatedUser user) {
		return roadmapService.getRoadmap(user);
	}
}