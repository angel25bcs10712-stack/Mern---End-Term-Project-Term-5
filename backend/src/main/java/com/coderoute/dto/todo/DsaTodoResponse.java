package com.coderoute.dto.todo;

import java.time.Instant;
import java.util.UUID;

import com.coderoute.entity.DsaTodo;

public record DsaTodoResponse(UUID id, String title, UUID problemId, boolean completed, Instant createdAt) {
	public static DsaTodoResponse from(DsaTodo todo) {
		return new DsaTodoResponse(todo.getId(), todo.getTitle(),
				todo.getProblem() == null ? null : todo.getProblem().getId(),
				Boolean.TRUE.equals(todo.getCompleted()), todo.getCreatedAt());
	}
}