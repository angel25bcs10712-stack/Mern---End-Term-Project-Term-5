package com.coderoute.dto.problem;

import java.util.List;
import java.util.UUID;

import com.coderoute.entity.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProblemCreateRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank @Size(max = 10000) String description,
		@NotNull Difficulty difficulty,
		@NotNull UUID topicId,
		@Size(max = 2048) @Pattern(regexp = "https?://[^\\s]+", message = "must be an HTTP or HTTPS URL") String externalUrl,
		@NotNull @Size(max = 20) List<@NotBlank @Size(max = 40) String> tags,
		@NotNull @Positive @Max(1440) Integer estimatedTimeMinutes) {
}