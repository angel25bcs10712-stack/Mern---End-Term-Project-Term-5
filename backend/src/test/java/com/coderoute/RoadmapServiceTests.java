package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.roadmap.RoadmapTopicStatus;
import com.coderoute.entity.Topic;
import com.coderoute.entity.TopicPrerequisite;
import com.coderoute.entity.User;
import com.coderoute.entity.UserTopicProgress;
import com.coderoute.entity.enums.Difficulty;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicPrerequisiteRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserTopicProgressRepository;
import com.coderoute.roadmap.RoadmapService;

class RoadmapServiceTests {
	private static final UUID USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final UUID ARRAYS_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID POINTERS_ID = UUID.fromString("20000000-0000-0000-0000-000000000004");

	@Test
	void attemptedTopicRemainsLockedUntilEveryPrerequisiteIsCompleted() {
		var fixture = fixture();
		UserTopicProgress pointerProgress = progress(POINTERS_ID, 0, 2, BigDecimal.ZERO);
		when(fixture.progressRepository.findAllByUser_Id(USER_ID)).thenReturn(List.of(pointerProgress));

		var roadmap = fixture.service.getRoadmap(principal());

		assertEquals(RoadmapTopicStatus.AVAILABLE, roadmap.topics().get(0).status());
		var pointers = roadmap.topics().stream().filter(topic -> topic.topicId().equals(POINTERS_ID)).findFirst().orElseThrow();
		assertEquals(RoadmapTopicStatus.LOCKED, pointers.status());
		assertEquals(1, pointers.prerequisites().size());
	}

	@Test
	void enoughSolvedProblemsUnlockTheNextTopicAndRecommendIt() {
		var fixture = fixture();
		UserTopicProgress arraysProgress = progress(ARRAYS_ID, 4, 5, BigDecimal.valueOf(80.00));
		when(fixture.progressRepository.findAllByUser_Id(USER_ID)).thenReturn(List.of(arraysProgress));

		var roadmap = fixture.service.getRoadmap(principal());

		assertEquals(RoadmapTopicStatus.COMPLETED, roadmap.topics().get(0).status());
		var pointers = roadmap.topics().stream().filter(topic -> topic.topicId().equals(POINTERS_ID)).findFirst().orElseThrow();
		assertEquals(RoadmapTopicStatus.AVAILABLE, pointers.status());
		assertEquals(List.of(POINTERS_ID), roadmap.nextRecommendedTopicIds());
		assertEquals(1, pointers.recommendationRank());
	}

	private Fixture fixture() {
		Topic arrays = topic(ARRAYS_ID, "Arrays");
		Topic pointers = topic(POINTERS_ID, "Two Pointers");
		TopicPrerequisite edge = mock(TopicPrerequisite.class);
		when(edge.getDependentTopic()).thenReturn(pointers);
		when(edge.getPrerequisiteTopic()).thenReturn(arrays);

		TopicRepository topicRepository = mock(TopicRepository.class);
		TopicPrerequisiteRepository prerequisiteRepository = mock(TopicPrerequisiteRepository.class);
		UserTopicProgressRepository progressRepository = mock(UserTopicProgressRepository.class);
		ProblemRepository problemRepository = mock(ProblemRepository.class);
		when(topicRepository.findAll(any(Sort.class))).thenReturn(List.of(arrays, pointers));
		when(prerequisiteRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(edge));
		when(progressRepository.findAllByUser_Id(USER_ID)).thenReturn(List.of());
		when(problemRepository.countProblemsByTopic()).thenReturn(List.of(
				new Object[] { ARRAYS_ID, 5L }, new Object[] { POINTERS_ID, 4L }));
		return new Fixture(new RoadmapService(topicRepository, prerequisiteRepository, progressRepository, problemRepository),
				progressRepository);
	}

	private Topic topic(UUID id, String name) {
		Topic topic = mock(Topic.class);
		when(topic.getId()).thenReturn(id);
		when(topic.getName()).thenReturn(name);
		when(topic.getDifficulty()).thenReturn(Difficulty.BEGINNER);
		return topic;
	}

	private UserTopicProgress progress(UUID topicId, int solved, int attempted, BigDecimal accuracy) {
		Topic topic = topic(topicId, topicId.equals(ARRAYS_ID) ? "Arrays" : "Two Pointers");
		UserTopicProgress progress = mock(UserTopicProgress.class);
		when(progress.getTopic()).thenReturn(topic);
		when(progress.getProblemsSolved()).thenReturn(solved);
		when(progress.getProblemsAttempted()).thenReturn(attempted);
		when(progress.getAccuracy()).thenReturn(accuracy);
		return progress;
	}

	private AuthenticatedUser principal() {
		User user = mock(User.class);
		when(user.getId()).thenReturn(USER_ID);
		return new AuthenticatedUser(user);
	}

	private record Fixture(RoadmapService service, UserTopicProgressRepository progressRepository) {
	}
}