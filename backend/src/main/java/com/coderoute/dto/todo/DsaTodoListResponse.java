package com.coderoute.dto.todo;

import java.util.List;

public record DsaTodoListResponse(
		List<DsaTodoResponse> items,
		int total,
		int completed,
		int remaining,
		int progressPercent) {
}