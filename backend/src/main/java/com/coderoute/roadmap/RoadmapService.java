package com.coderoute.roadmap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.roadmap.LearningRoadmapResponse;
import com.coderoute.dto.roadmap.RoadmapPrerequisiteResponse;
import com.coderoute.dto.roadmap.RoadmapTopicResponse;
import com.coderoute.dto.roadmap.RoadmapTopicStatus;
import com.coderoute.entity.Topic;
import com.coderoute.entity.TopicPrerequisite;
import com.coderoute.entity.UserTopicProgress;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicPrerequisiteRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserTopicProgressRepository;

@Service
public class RoadmapService {
	private static final double COMPLETION_FRACTION = 0.70;
	private static final double COMPLETION_ACCURACY = 70.0;
	private static final int RECOMMENDED_TOPIC_LIMIT = 3;

	private final TopicRepository topicRepository;
	private final TopicPrerequisiteRepository prerequisiteRepository;
	private final UserTopicProgressRepository progressRepository;
	private final ProblemRepository problemRepository;

	public RoadmapService(TopicRepository topicRepository, TopicPrerequisiteRepository prerequisiteRepository,
			UserTopicProgressRepository progressRepository, ProblemRepository problemRepository) {
		this.topicRepository = topicRepository;
		this.prerequisiteRepository = prerequisiteRepository;
		this.progressRepository = progressRepository;
		this.problemRepository = problemRepository;
	}

	@Transactional(readOnly = true)
	public LearningRoadmapResponse getRoadmap(AuthenticatedUser principal) {
		List<Topic> topics = topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
		Map<UUID, Topic> topicById = new HashMap<>();
		topics.forEach(topic -> topicById.put(topic.getId(), topic));

		List<TopicPrerequisite> storedEdges = prerequisiteRepository.findAllByOrderByCreatedAtAsc();
		List<TopicPrerequisiteGraph.Edge> edges = storedEdges.stream()
				.map(edge -> new TopicPrerequisiteGraph.Edge(edge.getDependentTopic().getId(), edge.getPrerequisiteTopic().getId()))
				.toList();
		TopicPrerequisiteGraph graph = new TopicPrerequisiteGraph(topicById.keySet(), edges);
		Map<UUID, Integer> problemCounts = loadProblemCounts();
		Map<UUID, UserTopicProgress> progressByTopic = new HashMap<>();
		progressRepository.findAllByUser_Id(principal.getId())
				.forEach(progress -> progressByTopic.put(progress.getTopic().getId(), progress));

		Map<UUID, ProgressSnapshot> snapshots = new HashMap<>();
		Set<UUID> completedIds = new HashSet<>();
		for (UUID topicId : graph.topologicalOrder()) {
			Topic topic = topicById.get(topicId);
			int total = problemCounts.getOrDefault(topicId, 0);
			UserTopicProgress progress = progressByTopic.get(topicId);
			int solved = progress == null ? 0 : progress.getProblemsSolved();
			int attempted = progress == null ? 0 : progress.getProblemsAttempted();
			BigDecimal accuracy = progress == null ? BigDecimal.ZERO.setScale(2) : progress.getAccuracy();
			int progressPercent = total == 0 ? 0 : Math.min(100, (int) Math.round(solved * 100.0 / total));
			int requiredSolved = Math.max(1, (int) Math.ceil(total * COMPLETION_FRACTION));
			boolean completed = total > 0 && solved >= requiredSolved && accuracy.doubleValue() >= COMPLETION_ACCURACY;
			boolean prerequisitesMet = graph.isAvailable(topicId, completedIds);
			RoadmapTopicStatus status = completed
					? RoadmapTopicStatus.COMPLETED
					: !prerequisitesMet
							? RoadmapTopicStatus.LOCKED
							: attempted > 0
									? RoadmapTopicStatus.IN_PROGRESS
									: RoadmapTopicStatus.AVAILABLE;
			if (completed) completedIds.add(topicId);
			double priority = priority(status, accuracy.doubleValue(), attempted, solved, graph.levelOf(topicId));
			snapshots.put(topicId, new ProgressSnapshot(total, solved, attempted, accuracy,
					progressPercent, status, priority));
		}

		List<UUID> recommendedIds = snapshots.entrySet().stream()
				.filter(entry -> entry.getValue().status() == RoadmapTopicStatus.AVAILABLE
						|| entry.getValue().status() == RoadmapTopicStatus.IN_PROGRESS)
				.sorted(Comparator.<Map.Entry<UUID, ProgressSnapshot>>comparingDouble(entry -> entry.getValue().priority())
						.reversed()
						.thenComparing(entry -> topicById.get(entry.getKey()).getName()))
				.limit(RECOMMENDED_TOPIC_LIMIT)
				.map(Map.Entry::getKey)
				.toList();
		Map<UUID, Integer> recommendationRanks = new HashMap<>();
		for (int i = 0; i < recommendedIds.size(); i++) recommendationRanks.put(recommendedIds.get(i), i + 1);

		List<RoadmapTopicResponse> nodes = new ArrayList<>();
		for (UUID topicId : graph.topologicalOrder()) {
			Topic topic = topicById.get(topicId);
			ProgressSnapshot snapshot = snapshots.get(topicId);
			List<RoadmapPrerequisiteResponse> prerequisites = graph.prerequisitesOf(topicId).stream()
					.map(id -> new RoadmapPrerequisiteResponse(id, topicById.get(id).getName()))
					.sorted(Comparator.comparing(RoadmapPrerequisiteResponse::topicName))
					.toList();
			int rank = recommendationRanks.getOrDefault(topicId, 0);
			double finitePriority = Double.isFinite(snapshot.priority()) ? snapshot.priority() : 0.0;
			nodes.add(new RoadmapTopicResponse(topicId, topic.getName(), topic.getDifficulty(), prerequisites,
					graph.levelOf(topicId), snapshot.totalProblems(), snapshot.problemsSolved(),
					snapshot.problemsAttempted(), snapshot.accuracy(), snapshot.progressPercent(), snapshot.status(),
					rank > 0, rank, BigDecimal.valueOf(finitePriority).setScale(2, RoundingMode.HALF_UP),
					nextStepReason(topic.getName(), snapshot)));
		}
		return new LearningRoadmapResponse(nodes, recommendedIds);
	}

	private Map<UUID, Integer> loadProblemCounts() {
		Map<UUID, Integer> counts = new HashMap<>();
		for (Object[] row : problemRepository.countProblemsByTopic()) {
			counts.put((UUID) row[0], Math.toIntExact((Long) row[1]));
		}
		return counts;
	}

	private double priority(RoadmapTopicStatus status, double accuracy, int attempted, int solved, int level) {
		if (status == RoadmapTopicStatus.IN_PROGRESS) {
			return 60.0 + (100.0 - accuracy) * 0.25 + Math.min(15, Math.max(0, attempted - solved) * 2.0);
		}
		if (status == RoadmapTopicStatus.AVAILABLE) return 45.0 - level;
		return Double.NEGATIVE_INFINITY;
	}

	private String nextStepReason(String topicName, ProgressSnapshot progress) {
		return switch (progress.status()) {
			case LOCKED -> "Finish the listed prerequisites to unlock " + topicName + ".";
			case COMPLETED -> "Completed: " + progress.problemsSolved() + " of " + progress.totalProblems()
					+ " problems solved with " + Math.round(progress.accuracy().doubleValue()) + "% accuracy.";
			case IN_PROGRESS -> "Continue strengthening this topic; current accuracy is "
					+ Math.round(progress.accuracy().doubleValue()) + "%.";
			case AVAILABLE -> "Prerequisites are complete. Start here to unlock the next topics.";
		};
	}

	private record ProgressSnapshot(int totalProblems, int problemsSolved, int problemsAttempted,
			BigDecimal accuracy, int progressPercent, RoadmapTopicStatus status, double priority) {
	}
}