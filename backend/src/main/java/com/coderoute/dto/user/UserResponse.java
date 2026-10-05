package com.coderoute.dto.user;

import java.time.Instant;
import java.util.UUID;

import com.coderoute.entity.enums.UserRole;

public record UserResponse(
		UUID id,
		String name,
		String email,
		UserRole role,
		Instant createdAt,
		Instant updatedAt) {
}