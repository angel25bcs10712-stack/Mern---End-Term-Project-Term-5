package com.coderoute.dto.goal;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LearningGoalCreateRequest(
		@NotNull @Positive Integer target,
		@NotNull @FutureOrPresent LocalDate targetDate,
		@NotNull @DecimalMin("0.50") @DecimalMax("168.00") BigDecimal weeklyHours) {
}