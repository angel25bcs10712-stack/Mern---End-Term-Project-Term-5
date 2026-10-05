package com.coderoute.dto.topic;

import java.time.Instant;
import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;

public record TopicResponse(
		UUID id,
		String name,
		String description,
		Difficulty difficulty,
		UUID parentTopicId,
		Instant createdAt,
		Instant updatedAt) {
}