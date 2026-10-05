package com.coderoute.dto.attempt;

import java.util.UUID;

import com.coderoute.entity.enums.AttemptStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProblemAttemptCreateRequest(
		@NotNull UUID problemId,
		@NotNull AttemptStatus status,
		@NotNull @PositiveOrZero Integer timeTakenSeconds,
		@NotNull @Positive Integer attempts) {
}