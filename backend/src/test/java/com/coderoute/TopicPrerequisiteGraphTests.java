package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.coderoute.roadmap.TopicPrerequisiteGraph;
import com.coderoute.roadmap.TopicPrerequisiteGraph.Edge;

class TopicPrerequisiteGraphTests {
	private final UUID arrays = id(1);
	private final UUID strings = id(2);
	private final UUID pointers = id(3);
	private final UUID window = id(4);

	@Test
	void topologicallyOrdersMultiplePrerequisitesAndUnlocksOnlyWhenComplete() {
		TopicPrerequisiteGraph graph = new TopicPrerequisiteGraph(
				List.of(arrays, strings, pointers, window),
				List.of(new Edge(pointers, arrays), new Edge(window, pointers), new Edge(window, strings)));

		assertEquals(0, graph.levelOf(arrays));
		assertEquals(0, graph.levelOf(strings));
		assertEquals(1, graph.levelOf(pointers));
		assertEquals(2, graph.levelOf(window));
		assertTrue(graph.topologicalOrder().indexOf(arrays) < graph.topologicalOrder().indexOf(pointers));
		assertTrue(graph.topologicalOrder().indexOf(pointers) < graph.topologicalOrder().indexOf(window));
		assertFalse(graph.isAvailable(window, Set.of(arrays, pointers)));
		assertTrue(graph.isAvailable(window, Set.of(arrays, pointers, strings)));
	}

	@Test
	void duplicateEdgesDoNotIncreasePrerequisiteCount() {
		TopicPrerequisiteGraph graph = new TopicPrerequisiteGraph(
				List.of(arrays, pointers), List.of(new Edge(pointers, arrays), new Edge(pointers, arrays)));

		assertEquals(Set.of(arrays), graph.prerequisitesOf(pointers));
		assertEquals(1, graph.levelOf(pointers));
	}

	@Test
	void rejectsCyclesAndReferencesToUnknownTopics() {
		assertThrows(IllegalArgumentException.class, () -> new TopicPrerequisiteGraph(
				List.of(arrays, pointers), List.of(new Edge(arrays, pointers), new Edge(pointers, arrays))));
		assertThrows(IllegalArgumentException.class, () -> new TopicPrerequisiteGraph(
				List.of(arrays), List.of(new Edge(arrays, pointers))));
	}

	private UUID id(long suffix) {
		return new UUID(0, suffix);
	}
}