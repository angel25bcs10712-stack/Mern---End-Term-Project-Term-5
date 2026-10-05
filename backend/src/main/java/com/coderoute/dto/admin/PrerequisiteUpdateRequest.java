package com.coderoute.dto.admin;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrerequisiteUpdateRequest(
		@NotNull @Size(max = 100) List<@NotNull UUID> prerequisiteTopicIds) {
}