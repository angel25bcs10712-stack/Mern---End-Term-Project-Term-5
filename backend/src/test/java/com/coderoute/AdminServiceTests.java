package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.coderoute.admin.AdminConflictException;
import com.coderoute.admin.AdminInputException;
import com.coderoute.admin.AdminService;
import com.coderoute.dto.admin.PrerequisiteUpdateRequest;
import com.coderoute.entity.Problem;
import com.coderoute.entity.Topic;
import com.coderoute.entity.TopicPrerequisite;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicPrerequisiteRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserRepository;
import com.coderoute.repository.UserTopicProgressRepository;

class AdminServiceTests {
	private final ProblemRepository problemRepository = mock(ProblemRepository.class);
	private final TopicRepository topicRepository = mock(TopicRepository.class);
	private final TopicPrerequisiteRepository prerequisiteRepository = mock(TopicPrerequisiteRepository.class);
	private final ProblemAttemptRepository attemptRepository = mock(ProblemAttemptRepository.class);
	private final UserTopicProgressRepository progressRepository = mock(UserTopicProgressRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final AdminService adminService = new AdminService(problemRepository, topicRepository,
			prerequisiteRepository, attemptRepository, progressRepository, userRepository);

	@Test
	void replacesPrerequisitesWhenTheGraphRemainsAcyclic() {
		UUID dependentId = UUID.randomUUID();
		UUID prerequisiteId = UUID.randomUUID();
		Topic dependent = topic(dependentId);
		Topic prerequisite = topic(prerequisiteId);
		when(topicRepository.findById(dependentId)).thenReturn(Optional.of(dependent));
		when(topicRepository.findAllById(any())).thenReturn(List.of(prerequisite));
		when(topicRepository.findAll()).thenReturn(List.of(dependent, prerequisite));
		when(prerequisiteRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of());
		when(prerequisiteRepository.findAllByDependentTopic_Id(dependentId)).thenReturn(List.of());

		adminService.updatePrerequisites(dependentId, new PrerequisiteUpdateRequest(List.of(prerequisiteId)));

		verify(prerequisiteRepository).saveAll(any());
	}

	@Test
	void rejectsPrerequisiteCyclesBeforeChangingStoredEdges() {
		UUID dependentId = UUID.randomUUID();
		UUID prerequisiteId = UUID.randomUUID();
		Topic dependent = topic(dependentId);
		Topic prerequisite = topic(prerequisiteId);
		TopicPrerequisite reverseEdge = mock(TopicPrerequisite.class);
		when(reverseEdge.getDependentTopic()).thenReturn(prerequisite);
		when(reverseEdge.getPrerequisiteTopic()).thenReturn(dependent);
		when(topicRepository.findById(dependentId)).thenReturn(Optional.of(dependent));
		when(topicRepository.findAllById(any())).thenReturn(List.of(prerequisite));
		when(topicRepository.findAll()).thenReturn(List.of(dependent, prerequisite));
		when(prerequisiteRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(reverseEdge));

		assertThrows(AdminInputException.class, () -> adminService.updatePrerequisites(dependentId,
				new PrerequisiteUpdateRequest(List.of(prerequisiteId))));

		verify(prerequisiteRepository, never()).deleteAll(anyList());
		verify(prerequisiteRepository, never()).saveAll(any());
	}

	@Test
	void refusesToDeleteProblemsReferencedByAttempts() {
		UUID problemId = UUID.randomUUID();
		when(problemRepository.findById(problemId)).thenReturn(Optional.of(mock(Problem.class)));
		when(attemptRepository.existsByProblem_Id(problemId)).thenReturn(true);

		assertThrows(AdminConflictException.class, () -> adminService.deleteProblem(problemId));

		verify(problemRepository, never()).delete(any(Problem.class));
	}

	private Topic topic(UUID id) {
		Topic topic = mock(Topic.class);
		when(topic.getId()).thenReturn(id);
		return topic;
	}
}