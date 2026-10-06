package com.coderoute.dto.todo;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DsaTodoCreateRequest(
		@NotBlank @Size(max = 200) String title,
		UUID problemId) {
}