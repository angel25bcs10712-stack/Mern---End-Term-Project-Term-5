package com.coderoute.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.dto.admin.AdminProblemResponse;
import com.coderoute.dto.admin.AdminStatsResponse;
import com.coderoute.dto.admin.AdminTopicResponse;
import com.coderoute.dto.admin.PrerequisiteUpdateRequest;
import com.coderoute.dto.problem.ProblemCreateRequest;
import com.coderoute.dto.topic.TopicRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {
	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/stats")
	public AdminStatsResponse stats() {
		return adminService.stats();
	}

	@GetMapping("/problems")
	public List<AdminProblemResponse> problems() {
		return adminService.problems();
	}

	@PostMapping("/problems")
	@ResponseStatus(HttpStatus.CREATED)
	public AdminProblemResponse createProblem(@Valid @RequestBody ProblemCreateRequest request) {
		return adminService.createProblem(request);
	}

	@PutMapping("/problems/{id}")
	public AdminProblemResponse updateProblem(@PathVariable UUID id, @Valid @RequestBody ProblemCreateRequest request) {
		return adminService.updateProblem(id, request);
	}

	@DeleteMapping("/problems/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProblem(@PathVariable UUID id) {
		adminService.deleteProblem(id);
	}

	@GetMapping("/topics")
	public List<AdminTopicResponse> topics() {
		return adminService.topics();
	}

	@PostMapping("/topics")
	@ResponseStatus(HttpStatus.CREATED)
	public AdminTopicResponse createTopic(@Valid @RequestBody TopicRequest request) {
		return adminService.createTopic(request);
	}

	@PutMapping("/topics/{id}")
	public AdminTopicResponse updateTopic(@PathVariable UUID id, @Valid @RequestBody TopicRequest request) {
		return adminService.updateTopic(id, request);
	}

	@DeleteMapping("/topics/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTopic(@PathVariable UUID id) {
		adminService.deleteTopic(id);
	}

	@PutMapping("/topics/{id}/prerequisites")
	public AdminTopicResponse updatePrerequisites(@PathVariable UUID id,
			@Valid @RequestBody PrerequisiteUpdateRequest request) {
		return adminService.updatePrerequisites(id, request);
	}
}