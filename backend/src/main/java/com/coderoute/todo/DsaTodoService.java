package com.coderoute.todo;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.todo.DsaTodoCreateRequest;
import com.coderoute.dto.todo.DsaTodoListResponse;
import com.coderoute.dto.todo.DsaTodoResponse;
import com.coderoute.entity.DsaTodo;
import com.coderoute.entity.Problem;
import com.coderoute.error.ResourceNotFoundException;
import com.coderoute.repository.DsaTodoRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.UserRepository;

@Service
public class DsaTodoService {
	private final DsaTodoRepository todoRepository;
	private final UserRepository userRepository;
	private final ProblemRepository problemRepository;

	public DsaTodoService(DsaTodoRepository todoRepository, UserRepository userRepository,
			ProblemRepository problemRepository) {
		this.todoRepository = todoRepository;
		this.userRepository = userRepository;
		this.problemRepository = problemRepository;
	}

	@Transactional(readOnly = true)
	public DsaTodoListResponse list(AuthenticatedUser principal) {
		List<DsaTodoResponse> items = todoRepository.findByUser_IdOrderByCreatedAtDesc(principal.getId()).stream()
				.map(DsaTodoResponse::from)
				.toList();
		int completed = (int) items.stream().filter(DsaTodoResponse::completed).count();
		int total = items.size();
		int remaining = total - completed;
		int progressPercent = total == 0 ? 0 : Math.round(completed * 100f / total);
		return new DsaTodoListResponse(items, total, completed, remaining, progressPercent);
	}

	@Transactional
	public DsaTodoResponse create(AuthenticatedUser principal, DsaTodoCreateRequest request) {
		var user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User"));
		String title = request.title().trim();
		Problem problem = null;
		if (request.problemId() != null) {
			problem = problemRepository.findById(request.problemId())
					.orElseThrow(() -> new ResourceNotFoundException("Problem"));
		}
		DsaTodo saved = todoRepository.save(new DsaTodo(user, title, problem));
		return DsaTodoResponse.from(saved);
	}

	@Transactional
	public DsaTodoResponse updateStatus(AuthenticatedUser principal, UUID id, boolean completed) {
		DsaTodo todo = findOwned(principal, id);
		todo.setCompleted(completed);
		return DsaTodoResponse.from(todoRepository.save(todo));
	}

	@Transactional
	public void delete(AuthenticatedUser principal, UUID id) {
		todoRepository.delete(findOwned(principal, id));
	}

	private DsaTodo findOwned(AuthenticatedUser principal, UUID id) {
		return todoRepository.findByIdAndUser_Id(id, principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("To-do item"));
	}
}
