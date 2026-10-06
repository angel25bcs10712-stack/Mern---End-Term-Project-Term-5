package com.coderoute.dto.auth;

import java.util.UUID;

import com.coderoute.entity.User;
import com.coderoute.entity.enums.UserRole;

public record UserResponse(UUID id, String name, String email, UserRole role, String leetcodeProfileUrl) {
	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
				user.getLeetcodeProfileUrl());
	}
}