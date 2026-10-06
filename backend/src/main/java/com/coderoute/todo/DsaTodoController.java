package com.coderoute.todo;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.todo.DsaTodoCreateRequest;
import com.coderoute.dto.todo.DsaTodoListResponse;
import com.coderoute.dto.todo.DsaTodoResponse;
import com.coderoute.dto.todo.DsaTodoStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me/todos")
public class DsaTodoController {
	private final DsaTodoService todoService;

	public DsaTodoController(DsaTodoService todoService) {
		this.todoService = todoService;
	}

	@GetMapping
	public DsaTodoListResponse list(@AuthenticationPrincipal AuthenticatedUser user) {
		return todoService.list(user);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DsaTodoResponse create(@Valid @RequestBody DsaTodoCreateRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return todoService.create(user, request);
	}

	@PatchMapping("/{id}")
	public DsaTodoResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody DsaTodoStatusRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return todoService.updateStatus(user, id, request.completed());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
		todoService.delete(user, id);
	}
}
