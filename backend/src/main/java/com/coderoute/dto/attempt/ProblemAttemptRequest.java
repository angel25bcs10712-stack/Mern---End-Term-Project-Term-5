package com.coderoute.dto.attempt;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProblemAttemptRequest(
		@NotNull @PositiveOrZero Integer timeTakenSeconds,
		@NotNull @Positive Integer attempts) {
}