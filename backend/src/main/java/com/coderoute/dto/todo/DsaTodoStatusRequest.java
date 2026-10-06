package com.coderoute.dto.todo;

import jakarta.validation.constraints.NotNull;

public record DsaTodoStatusRequest(@NotNull Boolean completed) {
}