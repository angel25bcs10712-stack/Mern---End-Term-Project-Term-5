package com.coderoute.problem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.attempt.ProblemAttemptRequest;
import com.coderoute.dto.attempt.ProblemAttemptResponse;
import com.coderoute.dto.problem.ProblemDetailResponse;
import com.coderoute.dto.problem.ProblemListItemResponse;
import com.coderoute.dto.problem.ProblemPageResponse;
import com.coderoute.dto.progress.TopicProgressResponse;
import com.coderoute.dto.progress.UserProgressResponse;
import com.coderoute.dto.topic.TopicResponse;
import com.coderoute.entity.Problem;
import com.coderoute.entity.ProblemAttempt;
import com.coderoute.entity.Topic;
import com.coderoute.entity.UserTopicProgress;
import com.coderoute.entity.enums.AttemptStatus;
import com.coderoute.entity.enums.Difficulty;
import com.coderoute.error.ResourceNotFoundException;
import com.coderoute.repository.DsaTodoRepository;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserRepository;
import com.coderoute.repository.UserTopicProgressRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class ProblemService {
	private final ProblemRepository problemRepository;
	private final TopicRepository topicRepository;
	private final ProblemAttemptRepository attemptRepository;
	private final UserRepository userRepository;
	private final UserTopicProgressRepository progressRepository;
	private final DsaTodoRepository todoRepository;

	public ProblemService(ProblemRepository problemRepository, TopicRepository topicRepository,
			ProblemAttemptRepository attemptRepository, UserRepository userRepository,
			UserTopicProgressRepository progressRepository, DsaTodoRepository todoRepository) {
		this.problemRepository = problemRepository;
		this.topicRepository = topicRepository;
		this.attemptRepository = attemptRepository;
		this.userRepository = userRepository;
		this.progressRepository = progressRepository;
		this.todoRepository = todoRepository;
	}

	@Transactional(readOnly = true)
	public ProblemPageResponse list(AuthenticatedUser principal, String search, UUID topicId,
			Difficulty difficulty, Boolean solved, int page, int size) {
		UUID userId = principal.getId();
		Specification<Problem> specification = (root, query, builder) -> {
			var predicates = new java.util.ArrayList<Predicate>();
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
				var topic = root.join("topic");
				predicates.add(builder.or(
						builder.like(builder.lower(root.get("title")), pattern),
						builder.like(builder.lower(root.get("description")), pattern),
						builder.like(builder.lower(topic.get("name")), pattern)));
			}
			if (topicId != null) predicates.add(builder.equal(root.get("topic").get("id"), topicId));
			if (difficulty != null) predicates.add(builder.equal(root.get("difficulty"), difficulty));
			if (solved != null) {
				var subquery = query.subquery(UUID.class);
				Root<ProblemAttempt> attempt = subquery.from(ProblemAttempt.class);
				subquery.select(attempt.get("problem").get("id"));
				subquery.where(
						builder.equal(attempt.get("user").get("id"), userId),
						builder.equal(attempt.get("problem").get("id"), root.get("id")),
						builder.equal(attempt.get("status"), AttemptStatus.SOLVED));
				predicates.add(solved ? builder.exists(subquery) : builder.not(builder.exists(subquery)));
			}
			return builder.and(predicates.toArray(Predicate[]::new));
		};

		Page<Problem> problems = problemRepository.findAll(specification,
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title")));
		List<UUID> problemIds = problems.getContent().stream().map(Problem::getId).toList();
		Set<UUID> solvedIds = problemIds.isEmpty() ? Set.of()
				: Set.copyOf(attemptRepository.findSolvedProblemIds(userId, problemIds));
		List<ProblemListItemResponse> results = problems.getContent().stream()
				.map(problem -> ProblemListItemResponse.from(problem, solvedIds.contains(problem.getId())))
				.toList();
		return ProblemPageResponse.from(new PageImpl<>(results, problems.getPageable(), problems.getTotalElements()));
	}

	@Transactional(readOnly = true)
	public ProblemDetailResponse get(UUID problemId, AuthenticatedUser principal) {
		Problem problem = findProblem(problemId);
		boolean solved = attemptRepository.existsByUser_IdAndProblem_IdAndStatus(
				principal.getId(), problemId, AttemptStatus.SOLVED);
		return ProblemDetailResponse.from(problem, solved);
	}

	@Transactional(readOnly = true)
	public List<TopicResponse> topics() {
		return topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
				.map(topic -> new TopicResponse(topic.getId(), topic.getName(), topic.getDescription(), topic.getDifficulty(),
						topic.getParentTopic() == null ? null : topic.getParentTopic().getId(),
						topic.getCreatedAt(), topic.getUpdatedAt()))
				.toList();
	}

	@Transactional
	public ProblemAttemptResponse record(UUID problemId, ProblemAttemptRequest request,
			AuthenticatedUser principal, boolean solved) {
		Problem problem = findProblem(problemId);
		var user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User"));
		AttemptStatus status = solved ? AttemptStatus.SOLVED : AttemptStatus.ATTEMPTED;
		ProblemAttempt attempt = attemptRepository.save(new ProblemAttempt(user, problem, status,
				request.timeTakenSeconds(), request.attempts(), solved ? Instant.now() : null));
		updateProgress(user.getId(), problem.getTopic(), problem.getDifficulty());
		if (solved) {
			// Keep the user's DSA to-do list in sync when a task matches this problem.
			todoRepository.completeMatchedForSolve(user.getId(), problem.getId(),
					problem.getTitle().trim(), Instant.now());
		}
		return new ProblemAttemptResponse(attempt.getId(), user.getId(), problem.getId(), attempt.getStatus(),
				attempt.getTimeTakenSeconds(), attempt.getAttempts(), attempt.getSolvedAt(),
				attempt.getCreatedAt(), attempt.getUpdatedAt());
	}

	@Transactional(readOnly = true)
	public UserProgressResponse progress(AuthenticatedUser principal) {
		UUID userId = principal.getId();
		Map<UUID, UserTopicProgress> progressByTopic = progressRepository.findAllByUser_Id(userId).stream()
				.collect(java.util.stream.Collectors.toMap(progress -> progress.getTopic().getId(), progress -> progress));
		List<TopicProgressResponse> topics = topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
				.map(topic -> {
					UserTopicProgress progress = progressByTopic.get(topic.getId());
					return progress == null
							? new TopicProgressResponse(topic.getId(), topic.getName(), 0, 0, BigDecimal.ZERO.setScale(2), null)
							: TopicProgressResponse.from(progress);
				})
				.toList();
		return new UserProgressResponse(problemRepository.count(), attemptRepository.countDistinctSolvedByUser(userId),
				attemptRepository.countDistinctProblemsByUser(userId), topics);
	}

	private void updateProgress(UUID userId, Topic topic, Difficulty difficulty) {
		long attempted = attemptRepository.countDistinctProblemsByUserAndTopic(userId, topic.getId());
		long solved = attemptRepository.countDistinctSolvedByUserAndTopic(userId, topic.getId());
		Double average = attemptRepository.averageTimeByUserAndTopic(userId, topic.getId());
		BigDecimal accuracy = attempted == 0 ? BigDecimal.ZERO
				: BigDecimal.valueOf(solved * 100.0 / attempted).setScale(2, RoundingMode.HALF_UP);
		BigDecimal averageTime = average == null ? null : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
		UserTopicProgress progress = progressRepository.findByUser_IdAndTopic_Id(userId, topic.getId())
				.orElseGet(() -> new UserTopicProgress(userRepository.getReferenceById(userId), topic));
		progress.updateSummary(Math.toIntExact(solved), Math.toIntExact(attempted), accuracy, averageTime, difficulty);
		progressRepository.save(progress);
	}

	private Problem findProblem(UUID problemId) {
		return problemRepository.findById(problemId)
				.orElseThrow(() -> new ResourceNotFoundException("Problem"));
	}
}