package com.coderoute.dto.auth;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(@Size(max = 255) String leetcodeProfileUrl) {
}