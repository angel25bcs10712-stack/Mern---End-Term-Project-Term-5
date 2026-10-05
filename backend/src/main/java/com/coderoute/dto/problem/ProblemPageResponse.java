package com.coderoute.dto.problem;

import java.util.List;

import org.springframework.data.domain.Page;

public record ProblemPageResponse(
		List<ProblemListItemResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {
	public static ProblemPageResponse from(Page<ProblemListItemResponse> page) {
		return new ProblemPageResponse(page.getContent(), page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}
}