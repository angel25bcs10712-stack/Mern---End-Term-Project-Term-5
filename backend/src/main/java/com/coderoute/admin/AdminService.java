package com.coderoute.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderoute.dto.admin.AdminProblemResponse;
import com.coderoute.dto.admin.AdminStatsResponse;
import com.coderoute.dto.admin.AdminTopicResponse;
import com.coderoute.dto.admin.PrerequisiteUpdateRequest;
import com.coderoute.dto.problem.ProblemCreateRequest;
import com.coderoute.dto.topic.TopicRequest;
import com.coderoute.entity.Problem;
import com.coderoute.entity.Topic;
import com.coderoute.entity.TopicPrerequisite;
import com.coderoute.entity.enums.AttemptStatus;
import com.coderoute.error.ResourceNotFoundException;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicPrerequisiteRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserRepository;
import com.coderoute.repository.UserTopicProgressRepository;
import com.coderoute.roadmap.TopicPrerequisiteGraph;

@Service
public class AdminService {
	private final ProblemRepository problemRepository;
	private final TopicRepository topicRepository;
	private final TopicPrerequisiteRepository prerequisiteRepository;
	private final ProblemAttemptRepository attemptRepository;
	private final UserTopicProgressRepository progressRepository;
	private final UserRepository userRepository;

	public AdminService(ProblemRepository problemRepository, TopicRepository topicRepository,
			TopicPrerequisiteRepository prerequisiteRepository, ProblemAttemptRepository attemptRepository,
			UserTopicProgressRepository progressRepository, UserRepository userRepository) {
		this.problemRepository = problemRepository;
		this.topicRepository = topicRepository;
		this.prerequisiteRepository = prerequisiteRepository;
		this.attemptRepository = attemptRepository;
		this.progressRepository = progressRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public AdminStatsResponse stats() {
		return new AdminStatsResponse(userRepository.count(), topicRepository.count(), problemRepository.count(),
				attemptRepository.count(), attemptRepository.countByStatus(AttemptStatus.SOLVED));
	}

	@Transactional(readOnly = true)
	public List<AdminProblemResponse> problems() {
		return problemRepository.findAll(Sort.by(Sort.Direction.ASC, "title")).stream()
				.map(AdminProblemResponse::from)
				.toList();
	}

	@Transactional
	public AdminProblemResponse createProblem(ProblemCreateRequest request) {
		Topic topic = findTopic(request.topicId());
		Problem problem = new Problem(request.title().trim(), request.description().trim(), request.difficulty(), topic,
				request.externalUrl(), request.tags().toArray(String[]::new), request.estimatedTimeMinutes());
		return AdminProblemResponse.from(problemRepository.save(problem));
	}

	@Transactional
	public AdminProblemResponse updateProblem(UUID id, ProblemCreateRequest request) {
		Problem problem = problemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Problem"));
		problem.update(request.title().trim(), request.description().trim(), request.difficulty(),
				findTopic(request.topicId()), request.externalUrl(), request.tags().toArray(String[]::new),
				request.estimatedTimeMinutes());
		return AdminProblemResponse.from(problem);
	}

	@Transactional
	public void deleteProblem(UUID id) {
		Problem problem = problemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Problem"));
		if (attemptRepository.existsByProblem_Id(id)) {
			throw new AdminConflictException("This problem has attempts and cannot be deleted.");
		}
		problemRepository.delete(problem);
	}

	@Transactional(readOnly = true)
	public List<AdminTopicResponse> topics() {
		List<TopicPrerequisite> edges = prerequisiteRepository.findAllByOrderByCreatedAtAsc();
		Map<UUID, List<UUID>> prerequisiteIds = prerequisitesByTopic(edges);
		return topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
				.map(topic -> AdminTopicResponse.from(topic, prerequisiteIds.getOrDefault(topic.getId(), List.of())))
				.toList();
	}

	@Transactional
	public AdminTopicResponse createTopic(TopicRequest request) {
		ensureTopicNameAvailable(request.name(), null);
		Topic topic = topicRepository.save(new Topic(request.name().trim(), normalizeDescription(request.description()),
				request.difficulty(), findParent(request.parentTopicId(), null)));
		return AdminTopicResponse.from(topic, List.of());
	}

	@Transactional
	public AdminTopicResponse updateTopic(UUID id, TopicRequest request) {
		Topic topic = findTopic(id);
		ensureTopicNameAvailable(request.name(), id);
		Topic parent = findParent(request.parentTopicId(), id);
		topic.update(request.name().trim(), normalizeDescription(request.description()), request.difficulty(), parent);
		Map<UUID, List<UUID>> prerequisiteIds = prerequisitesByTopic(prerequisiteRepository.findAllByOrderByCreatedAtAsc());
		return AdminTopicResponse.from(topic, prerequisiteIds.getOrDefault(id, List.of()));
	}

	@Transactional
	public void deleteTopic(UUID id) {
		Topic topic = findTopic(id);
		if (problemRepository.existsByTopic_Id(id) || topicRepository.existsByParentTopic_Id(id)
				|| prerequisiteRepository.existsByDependentTopic_Id(id)
				|| prerequisiteRepository.existsByPrerequisiteTopic_Id(id) || progressRepository.existsByTopic_Id(id)) {
			throw new AdminConflictException("This topic is referenced by catalog or progress data and cannot be deleted.");
		}
		topicRepository.delete(topic);
	}

	@Transactional
	public AdminTopicResponse updatePrerequisites(UUID topicId, PrerequisiteUpdateRequest request) {
		Topic dependentTopic = findTopic(topicId);
		List<UUID> requestedIds = request.prerequisiteTopicIds();
		Set<UUID> uniqueIds = new LinkedHashSet<>(requestedIds);
		if (uniqueIds.size() != requestedIds.size()) {
			throw new AdminInputException("Prerequisite topic IDs must be unique.");
		}

		List<Topic> selectedTopics = topicRepository.findAllById(uniqueIds);
		Map<UUID, Topic> topicsById = new HashMap<>();
		selectedTopics.forEach(topic -> topicsById.put(topic.getId(), topic));
		if (topicsById.size() != uniqueIds.size()) {
			throw new AdminInputException("Every prerequisite must reference an existing topic.");
		}

		List<TopicPrerequisite> allEdges = prerequisiteRepository.findAllByOrderByCreatedAtAsc();
		List<TopicPrerequisiteGraph.Edge> candidateEdges = new ArrayList<>();
		allEdges.stream().filter(edge -> !edge.getDependentTopic().getId().equals(topicId))
				.forEach(edge -> candidateEdges.add(new TopicPrerequisiteGraph.Edge(
						edge.getDependentTopic().getId(), edge.getPrerequisiteTopic().getId())));
		uniqueIds.forEach(prerequisiteId -> candidateEdges.add(new TopicPrerequisiteGraph.Edge(topicId, prerequisiteId)));
		try {
			new TopicPrerequisiteGraph(topicRepository.findAll().stream().map(Topic::getId).toList(), candidateEdges);
		} catch (IllegalArgumentException exception) {
			throw new AdminInputException(exception.getMessage());
		}

		List<TopicPrerequisite> previousEdges = prerequisiteRepository.findAllByDependentTopic_Id(topicId);
		prerequisiteRepository.deleteAll(previousEdges);
		List<TopicPrerequisite> newEdges = uniqueIds.stream()
				.map(prerequisiteId -> new TopicPrerequisite(dependentTopic, topicsById.get(prerequisiteId)))
				.toList();
		prerequisiteRepository.saveAll(newEdges);
		return AdminTopicResponse.from(dependentTopic, uniqueIds.stream().sorted().toList());
	}

	private Topic findTopic(UUID id) {
		return topicRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Topic"));
	}

	private Topic findParent(UUID parentId, UUID topicId) {
		if (parentId == null) return null;
		if (parentId.equals(topicId)) throw new AdminInputException("A topic cannot be its own parent.");
		Topic parent = findTopic(parentId);
		Set<UUID> seen = new HashSet<>();
		Topic ancestor = parent;
		while (ancestor != null) {
			if (!seen.add(ancestor.getId()) || ancestor.getId().equals(topicId)) {
				throw new AdminInputException("Topic parent relationships must not contain a cycle.");
			}
			ancestor = ancestor.getParentTopic();
		}
		return parent;
	}

	private void ensureTopicNameAvailable(String name, UUID currentTopicId) {
		for (Topic existing : topicRepository.findAll()) {
			if (existing.getName().equalsIgnoreCase(name.trim()) && !existing.getId().equals(currentTopicId)) {
				throw new AdminConflictException("A topic with this name already exists.");
			}
		}
	}

	private String normalizeDescription(String description) {
		return description == null || description.isBlank() ? null : description.trim();
	}

	private Map<UUID, List<UUID>> prerequisitesByTopic(List<TopicPrerequisite> edges) {
		Map<UUID, List<UUID>> result = new HashMap<>();
		edges.stream().sorted(Comparator.comparing(edge -> edge.getPrerequisiteTopic().getName()))
				.forEach(edge -> result.computeIfAbsent(edge.getDependentTopic().getId(), ignored -> new ArrayList<>())
						.add(edge.getPrerequisiteTopic().getId()));
		return result;
	}
}