package com.coderoute.roadmap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TopicPrerequisiteGraph {
	private final Map<UUID, Set<UUID>> prerequisitesByTopic;
	private final Map<UUID, Integer> levelByTopic;
	private final List<UUID> topologicalOrder;

	public TopicPrerequisiteGraph(Collection<UUID> topicIds, Collection<Edge> edges) {
		Map<UUID, Set<UUID>> prerequisites = new LinkedHashMap<>();
		Map<UUID, Set<UUID>> dependents = new LinkedHashMap<>();
		Map<UUID, Integer> incomingCount = new LinkedHashMap<>();
		for (UUID topicId : topicIds) {
			if (prerequisites.putIfAbsent(topicId, new LinkedHashSet<>()) != null) {
				throw new IllegalArgumentException("Topic IDs must be unique");
			}
			dependents.put(topicId, new LinkedHashSet<>());
			incomingCount.put(topicId, 0);
		}
		for (Edge edge : edges) {
			if (!prerequisites.containsKey(edge.topicId()) || !prerequisites.containsKey(edge.prerequisiteId())) {
				throw new IllegalArgumentException("Prerequisite edges must reference known topics");
			}
			if (edge.topicId().equals(edge.prerequisiteId())) {
				throw new IllegalArgumentException("A topic cannot depend on itself");
			}
			if (prerequisites.get(edge.topicId()).add(edge.prerequisiteId())) {
				dependents.get(edge.prerequisiteId()).add(edge.topicId());
				incomingCount.compute(edge.topicId(), (ignored, count) -> count + 1);
			}
		}

		Map<UUID, Integer> levels = new HashMap<>();
		ArrayDeque<UUID> ready = new ArrayDeque<>();
		incomingCount.forEach((topicId, count) -> {
			levels.put(topicId, 0);
			if (count == 0) ready.addLast(topicId);
		});
		List<UUID> ordered = new ArrayList<>(topicIds.size());
		while (!ready.isEmpty()) {
			UUID prerequisiteId = ready.removeFirst();
			ordered.add(prerequisiteId);
			for (UUID dependentId : dependents.get(prerequisiteId)) {
				levels.compute(dependentId, (ignored, current) -> Math.max(current, levels.get(prerequisiteId) + 1));
				int remaining = incomingCount.compute(dependentId, (ignored, count) -> count - 1);
				if (remaining == 0) ready.addLast(dependentId);
			}
		}
		if (ordered.size() != topicIds.size()) {
			throw new IllegalArgumentException("Topic prerequisites must form a directed acyclic graph");
		}

		Map<UUID, Set<UUID>> immutablePrerequisites = new LinkedHashMap<>();
		prerequisites.forEach((topicId, required) -> immutablePrerequisites.put(topicId, Set.copyOf(required)));
		this.prerequisitesByTopic = Map.copyOf(immutablePrerequisites);
		this.levelByTopic = Map.copyOf(levels);
		this.topologicalOrder = List.copyOf(ordered);
	}

	public Set<UUID> prerequisitesOf(UUID topicId) {
		return prerequisitesByTopic.getOrDefault(topicId, Set.of());
	}

	public int levelOf(UUID topicId) {
		Integer level = levelByTopic.get(topicId);
		if (level == null) throw new IllegalArgumentException("Unknown topic: " + topicId);
		return level;
	}

	public List<UUID> topologicalOrder() {
		return topologicalOrder;
	}

	public boolean isAvailable(UUID topicId, Set<UUID> completedTopicIds) {
		return completedTopicIds.containsAll(prerequisitesOf(topicId));
	}

	public record Edge(UUID topicId, UUID prerequisiteId) {
	}
}