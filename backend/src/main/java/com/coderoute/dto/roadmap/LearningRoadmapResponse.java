package com.coderoute.dto.roadmap;

import java.util.List;
import java.util.UUID;

public record LearningRoadmapResponse(
		List<RoadmapTopicResponse> topics,
		List<UUID> nextRecommendedTopicIds) {
}