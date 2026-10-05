package com.coderoute.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.problem.ProblemListItemResponse;
import com.coderoute.dto.recommendation.RecommendationResponse;
import com.coderoute.entity.LearningGoal;
import com.coderoute.entity.Problem;
import com.coderoute.entity.ProblemAttempt;
import com.coderoute.entity.Topic;
import com.coderoute.entity.UserTopicProgress;
import com.coderoute.entity.enums.AttemptStatus;
import com.coderoute.entity.enums.Difficulty;
import com.coderoute.entity.enums.GoalStatus;
import com.coderoute.repository.LearningGoalRepository;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.UserTopicProgressRepository;

@Service
public class RecommendationService {
	private static final int RESULT_LIMIT = 5;
	private static final int RECENT_ATTEMPT_LIMIT = 100;
	private static final int RECENT_FAILURE_DAYS = 14;
	private static final Map<String, Set<String>> TOPIC_PREREQUISITES = Map.ofEntries(
			Map.entry("two pointers", Set.of("arrays")),
			Map.entry("sliding window", Set.of("arrays", "strings")),
			Map.entry("stack", Set.of("arrays")),
			Map.entry("queue", Set.of("arrays")),
			Map.entry("linked list", Set.of("arrays")),
			Map.entry("binary search", Set.of("arrays")),
			Map.entry("trees", Set.of("stack", "queue")),
			Map.entry("graphs", Set.of("stack", "queue")),
			Map.entry("greedy", Set.of("arrays")),
			Map.entry("backtracking", Set.of("trees")),
			Map.entry("dynamic programming", Set.of("arrays")));

	private final ProblemRepository problemRepository;
	private final ProblemAttemptRepository attemptRepository;
	private final UserTopicProgressRepository progressRepository;
	private final LearningGoalRepository goalRepository;
	private final RecommendationScoringService scoringService;

	public RecommendationService(ProblemRepository problemRepository, ProblemAttemptRepository attemptRepository,
			UserTopicProgressRepository progressRepository, LearningGoalRepository goalRepository,
			RecommendationScoringService scoringService) {
		this.problemRepository = problemRepository;
		this.attemptRepository = attemptRepository;
		this.progressRepository = progressRepository;
		this.goalRepository = goalRepository;
		this.scoringService = scoringService;
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> recommend(AuthenticatedUser principal) {
		UUID userId = principal.getId();
		Instant now = Instant.now();
		List<Problem> problems = problemRepository.findAll(Sort.by(Sort.Direction.ASC, "title"));
		if (problems.isEmpty()) return List.of();

		List<UUID> problemIds = problems.stream().map(Problem::getId).toList();
		Set<UUID> solvedIds = Set.copyOf(attemptRepository.findSolvedProblemIds(userId, problemIds));
		List<ProblemAttempt> recentAttempts = attemptRepository.findTop100ByUser_IdOrderByCreatedAtDesc(userId);
		Map<UUID, UserTopicProgress> progressByTopicId = new HashMap<>();
		Map<String, UserTopicProgress> progressByTopicName = new HashMap<>();
		for (UserTopicProgress progress : progressRepository.findAllByUser_Id(userId)) {
			progressByTopicId.put(progress.getTopic().getId(), progress);
			progressByTopicName.put(normalize(progress.getTopic().getName()), progress);
		}
		Map<UUID, ProblemAttempt> latestAttemptByProblem = new HashMap<>();
		Map<UUID, List<ProblemAttempt>> recentFailuresByTopic = new HashMap<>();
		Instant failureCutoff = now.minus(RECENT_FAILURE_DAYS, ChronoUnit.DAYS);
		for (ProblemAttempt attempt : recentAttempts) {
			latestAttemptByProblem.putIfAbsent(attempt.getProblem().getId(), attempt);
			if (attempt.getStatus() != AttemptStatus.SOLVED && !attempt.getCreatedAt().isBefore(failureCutoff)) {
				recentFailuresByTopic.computeIfAbsent(attempt.getProblem().getTopic().getId(), ignored -> new java.util.ArrayList<>())
						.add(attempt);
			}
		}

		LearningGoal activeGoal = goalRepository
				.findFirstByUser_IdAndStatusOrderByTargetDateAsc(userId, GoalStatus.ACTIVE).orElse(null);
		long problemsSolved = attemptRepository.countDistinctSolvedByUser(userId);
		double goalUrgency = calculateGoalUrgency(activeGoal, problemsSolved);
		List<ScoredProblem> scored = problems.stream()
				.filter(problem -> !solvedIds.contains(problem.getId()))
				.map(problem -> score(problem, progressByTopicId, progressByTopicName,
						recentFailuresByTopic, latestAttemptByProblem, activeGoal != null, goalUrgency, now))
				.sorted(Comparator.comparingDouble(ScoredProblem::score).reversed()
						.thenComparing(item -> item.problem().getTitle()))
				.limit(RESULT_LIMIT)
				.toList();

		return scored.stream().map(item -> new RecommendationResponse(
				ProblemListItemResponse.from(item.problem(), false),
				scoringService.reason(item.signals()),
				BigDecimal.valueOf(item.score()).setScale(2, RoundingMode.HALF_UP),
				item.problem().getTopic().getName(),
				item.problem().getDifficulty())).toList();
	}

	private ScoredProblem score(Problem problem, Map<UUID, UserTopicProgress> progressByTopicId,
			Map<String, UserTopicProgress> progressByTopicName,
			Map<UUID, List<ProblemAttempt>> recentFailuresByTopic,
			Map<UUID, ProblemAttempt> latestAttemptByProblem,
			boolean hasActiveGoal, double goalUrgency, Instant now) {
		UUID topicId = problem.getTopic().getId();
		UserTopicProgress progress = progressByTopicId.get(topicId);
		List<ProblemAttempt> failures = recentFailuresByTopic.getOrDefault(topicId, List.of());
		int highestFailureAttempts = failures.stream().mapToInt(ProblemAttempt::getAttempts).max().orElse(0);
		Difficulty recentFailureDifficulty = failures.stream()
				.collect(java.util.stream.Collectors.groupingBy(attempt -> attempt.getProblem().getDifficulty(),
						java.util.stream.Collectors.counting()))
				.entrySet().stream()
				.max(Map.Entry.<Difficulty, Long>comparingByValue().thenComparing(entry -> entry.getKey().ordinal()))
				.map(Map.Entry::getKey).orElse(null);
		Set<String> prerequisites = prerequisitesFor(problem.getTopic());
		int masteredPrerequisites = (int) prerequisites.stream()
				.filter(name -> isMastered(progressByTopicName.get(name))).count();
		ProblemAttempt lastAttempt = latestAttemptByProblem.get(problem.getId());
		var signals = new RecommendationScoringService.CandidateSignals(
				problem.getTopic().getName(), problem.getDifficulty(), problem.getEstimatedTimeMinutes(), false,
				progress == null ? null : progress.getAccuracy().doubleValue(),
				progress == null ? null : progress.getCurrentDifficulty(),
				progress == null ? 0 : progress.getProblemsAttempted(),
				failures.size(), highestFailureAttempts, recentFailureDifficulty,
				lastAttempt == null ? null : lastAttempt.getCreatedAt(),
				progress == null || progress.getAverageTimeSeconds() == null
						? null : progress.getAverageTimeSeconds().doubleValue(),
				masteredPrerequisites, prerequisites.size(), hasActiveGoal, goalUrgency);
		return new ScoredProblem(problem, signals, scoringService.score(signals, now));
	}

	private Set<String> prerequisitesFor(Topic topic) {
		Set<String> prerequisites = new HashSet<>(TOPIC_PREREQUISITES.getOrDefault(normalize(topic.getName()), Set.of()));
		if (topic.getParentTopic() != null) prerequisites.add(normalize(topic.getParentTopic().getName()));
		return prerequisites;
	}

	private boolean isMastered(UserTopicProgress progress) {
		return progress != null && progress.getProblemsSolved() > 0 && progress.getAccuracy().doubleValue() >= 60.0;
	}

	private double calculateGoalUrgency(LearningGoal goal, long problemsSolved) {
		if (goal == null) return 0.0;
		long remaining = Math.max(0, goal.getTarget() - problemsSolved);
		if (remaining == 0) return 0.0;
		long daysLeft = Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate()) + 1);
		double requiredPerDay = remaining / (double) daysLeft;
		double expectedPerDay = Math.max(0.2, (goal.getWeeklyHours().doubleValue() * 60.0 / 45.0) / 7.0);
		return Math.min(1.0, requiredPerDay / expectedPerDay);
	}

	private String normalize(String topicName) {
		return topicName.toLowerCase(Locale.ROOT);
	}

	private record ScoredProblem(Problem problem, RecommendationScoringService.CandidateSignals signals, double score) {
	}
}