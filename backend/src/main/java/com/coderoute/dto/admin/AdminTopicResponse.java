package com.coderoute.dto.admin;

import java.util.List;
import java.util.UUID;

import com.coderoute.entity.Topic;
import com.coderoute.entity.enums.Difficulty;

public record AdminTopicResponse(
		UUID id,
		String name,
		String description,
		Difficulty difficulty,
		UUID parentTopicId,
		List<UUID> prerequisiteTopicIds) {
	public static AdminTopicResponse from(Topic topic, List<UUID> prerequisiteTopicIds) {
		return new AdminTopicResponse(topic.getId(), topic.getName(), topic.getDescription(), topic.getDifficulty(),
				topic.getParentTopic() == null ? null : topic.getParentTopic().getId(), prerequisiteTopicIds);
	}
}