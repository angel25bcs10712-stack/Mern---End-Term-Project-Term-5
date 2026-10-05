package com.coderoute.dto.auth;

public record AuthResponse(String token, UserResponse user) {
}