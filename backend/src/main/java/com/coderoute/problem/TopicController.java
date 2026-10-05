package com.coderoute.problem;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.dto.topic.TopicResponse;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
	private final ProblemService problemService;

	public TopicController(ProblemService problemService) {
		this.problemService = problemService;
	}

	@GetMapping
	public List<TopicResponse> list() {
		return problemService.topics();
	}
}