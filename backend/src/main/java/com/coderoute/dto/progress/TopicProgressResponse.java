package com.coderoute.dto.progress;

import java.math.BigDecimal;
import java.util.UUID;

import com.coderoute.entity.UserTopicProgress;

public record TopicProgressResponse(
		UUID topicId,
		String topicName,
		int problemsSolved,
		int problemsAttempted,
		BigDecimal accuracy,
		BigDecimal averageTimeSeconds) {
	public static TopicProgressResponse from(UserTopicProgress progress) {
		return new TopicProgressResponse(progress.getTopic().getId(), progress.getTopic().getName(),
				progress.getProblemsSolved(), progress.getProblemsAttempted(), progress.getAccuracy(),
				progress.getAverageTimeSeconds());
	}
}