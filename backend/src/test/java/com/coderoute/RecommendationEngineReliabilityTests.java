package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.attempt.ProblemAttemptRequest;
import com.coderoute.dto.recommendation.RecommendationResponse;
import com.coderoute.entity.Problem;
import com.coderoute.entity.Topic;
import com.coderoute.entity.User;
import com.coderoute.entity.enums.Difficulty;
import com.coderoute.entity.enums.UserRole;
import com.coderoute.problem.ProblemService;
import com.coderoute.recommendation.RecommendationService;
import com.coderoute.repository.LearningGoalRepository;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserRepository;
import com.coderoute.repository.UserTopicProgressRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:recommendation-reliability-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=recommendation-test-secret-that-is-at-least-32-bytes"
})
class RecommendationEngineReliabilityTests {

	@Autowired
	private RecommendationService recommendationService;

	@Autowired
	private ProblemService problemService;

	@Autowired
	private ProblemRepository problemRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private ProblemAttemptRepository attemptRepository;

	@Autowired
	private UserTopicProgressRepository progressRepository;

	@Autowired
	private LearningGoalRepository goalRepository;

	@Autowired
	private UserRepository userRepository;

	private User freshUser;
	private User practicingUser;
	private Topic arraysTopic;
	private Topic treesTopic;
	private Problem probArrays1;
	private Problem probArrays2;
	private Problem probTrees1;

	@BeforeEach
	void setUp() {
		goalRepository.deleteAll();
		progressRepository.deleteAll();
		attemptRepository.deleteAll();
		problemRepository.deleteAll();
		topicRepository.deleteAll();
		userRepository.deleteAll();

		freshUser = userRepository.save(new User("Fresh User", "fresh@example.com", "hash", UserRole.USER));
		practicingUser = userRepository.save(new User("Practicing User", "practicing@example.com", "hash", UserRole.USER));

		arraysTopic = topicRepository.save(new Topic("Arrays", "Arrays and Lists", Difficulty.BEGINNER, null));
		treesTopic = topicRepository.save(new Topic("Trees", "Binary Trees", Difficulty.INTERMEDIATE, null));

		probArrays1 = problemRepository.save(new Problem(
				"Two Sum Easy", "Find indices summing to target", Difficulty.BEGINNER,
				arraysTopic, null, new String[] { "arrays" }, 15));
		probArrays2 = problemRepository.save(new Problem(
				"Three Sum Medium", "Find unique triplets summing to 0", Difficulty.INTERMEDIATE,
				arraysTopic, null, new String[] { "arrays", "two-pointers" }, 30));
		probTrees1 = problemRepository.save(new Problem(
				"Invert Binary Tree", "Invert a binary tree", Difficulty.BEGINNER,
				treesTopic, null, new String[] { "trees" }, 20));
	}

	@Test
	@DisplayName("Recommendation with no history: generates valid introductory recommendations with sorted scores")
	void recommend_noHistory_returnsIntroductoryRecommendations() {
		AuthenticatedUser principal = new AuthenticatedUser(freshUser);

		List<RecommendationResponse> recommendations = recommendationService.recommend(principal);

		assertNotNull(recommendations);
		assertFalse(recommendations.isEmpty(), "Recommendations should not be empty for a catalog with problems");
		assertTrue(recommendations.size() <= 5);

		// Every recommendation should have a positive score and a descriptive introductory explanation
		for (RecommendationResponse rec : recommendations) {
			assertNotNull(rec.problem());
			assertNotNull(rec.problem().title());
			assertNotNull(rec.recommendationScore());
			assertTrue(rec.recommendationScore().doubleValue() > 0);
			assertNotNull(rec.reasonForRecommendation());
			assertTrue(rec.reasonForRecommendation().contains("introduction") || rec.reasonForRecommendation().contains("useful starting point"),
					"Expected introductory explanation for fresh user, got: " + rec.reasonForRecommendation());
		}

		// Verify descending score order
		for (int i = 0; i < recommendations.size() - 1; i++) {
			assertTrue(
					recommendations.get(i).recommendationScore().compareTo(recommendations.get(i + 1).recommendationScore()) >= 0,
					"Recommendations must be sorted descending by recommendation score");
		}
	}

	@Test
	@DisplayName("Recommendation with limited history: excludes solved problems and adapts based on user practice")
	void recommend_limitedHistory_excludesSolvedProblems() {
		AuthenticatedUser principal = new AuthenticatedUser(practicingUser);

		// User solves probArrays1
		problemService.record(probArrays1.getId(), new ProblemAttemptRequest(300, 1), principal, true);

		// User attempts probTrees1 without solving
		problemService.record(probTrees1.getId(), new ProblemAttemptRequest(600, 2), principal, false);

		List<RecommendationResponse> recommendations = recommendationService.recommend(principal);

		assertNotNull(recommendations);
		assertFalse(recommendations.isEmpty());

		Set<UUID> recommendedIds = recommendations.stream()
				.map(r -> r.problem().id())
				.collect(Collectors.toSet());

		// Crucial reliability invariant: probArrays1 is solved, so it MUST NOT be in recommendations!
		assertFalse(recommendedIds.contains(probArrays1.getId()),
				"Already solved problem must never be recommended to the user");

		// probArrays2 and probTrees1 are eligible candidates
		assertTrue(recommendedIds.contains(probArrays2.getId()) || recommendedIds.contains(probTrees1.getId()),
				"Unsolved problems should be recommended");
	}

	@Test
	@DisplayName("Recommendation when all problems are solved: returns empty list gracefully without throwing")
	void recommend_allProblemsSolved_returnsEmptyList() {
		AuthenticatedUser principal = new AuthenticatedUser(practicingUser);

		// Solve all problems in database
		problemService.record(probArrays1.getId(), new ProblemAttemptRequest(300, 1), principal, true);
		problemService.record(probArrays2.getId(), new ProblemAttemptRequest(400, 1), principal, true);
		problemService.record(probTrees1.getId(), new ProblemAttemptRequest(500, 1), principal, true);

		List<RecommendationResponse> recommendations = recommendationService.recommend(principal);

		assertNotNull(recommendations);
		assertTrue(recommendations.isEmpty(),
				"When all problems are solved, recommendation list should be empty");
	}
}
