package com.coderoute.dto.topic;

import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TopicRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 2000) String description,
		@NotNull Difficulty difficulty,
		UUID parentTopicId) {
}