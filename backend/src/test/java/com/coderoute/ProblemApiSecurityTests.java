package com.coderoute;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.coderoute.auth.AuthenticatedUser;
import com.coderoute.dto.attempt.ProblemAttemptResponse;
import com.coderoute.dto.problem.ProblemPageResponse;
import com.coderoute.dto.progress.UserProgressResponse;
import com.coderoute.dto.problem.ProblemListItemResponse;
import com.coderoute.dto.recommendation.RecommendationResponse;
import com.coderoute.dto.roadmap.LearningRoadmapResponse;
import com.coderoute.dto.roadmap.RoadmapTopicResponse;
import com.coderoute.dto.roadmap.RoadmapTopicStatus;
import com.coderoute.entity.enums.AttemptStatus;
import com.coderoute.entity.enums.Difficulty;
import com.coderoute.problem.ProblemService;
import com.coderoute.recommendation.RecommendationService;
import com.coderoute.roadmap.RoadmapService;
import com.coderoute.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:problem-api-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=problem-api-test-secret-that-is-more-than-32-bytes"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ProblemApiSecurityTests {
	private static final String FIRST_USER = """
			{"name":"Riya Sen","email":"riya@example.com","password":"RoutePass123"}
			""";
	private static final String SECOND_USER = """
			{"name":"Noah Park","email":"noah@example.com","password":"RoutePass123"}
			""";
	private final UUID problemId = UUID.fromString("30000000-0000-0000-0000-000000000001");
	private final UUID topicId = UUID.fromString("20000000-0000-0000-0000-000000000001");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private ProblemService problemService;

	@MockitoBean
	private RecommendationService recommendationService;

	@MockitoBean
	private RoadmapService roadmapService;

	@BeforeEach
	void clearUsers() {
		userRepository.deleteAll();
		org.mockito.Mockito.reset(problemService);
	}

	@Test
	void problemRoutesRequireAuthenticationAndAcceptPagedFilters() throws Exception {
		mockMvc.perform(get("/api/problems")).andExpect(status().isUnauthorized());
		String token = register(FIRST_USER);
		when(problemService.list(any(AuthenticatedUser.class), eq("tree"), eq(topicId),
				eq(Difficulty.INTERMEDIATE), eq(true), eq(1), eq(10)))
				.thenReturn(new ProblemPageResponse(List.of(), 1, 10, 0, 0));

		mockMvc.perform(get("/api/problems")
				.param("search", "tree")
				.param("topicId", topicId.toString())
				.param("difficulty", "INTERMEDIATE")
				.param("solved", "true")
				.param("page", "1")
				.param("size", "10")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(10));
	}

	@Test
	void attemptIdentityComesFromJwtAndNotRequestBody() throws Exception {
		String ownerToken = register(FIRST_USER);
		register(SECOND_USER);
		UUID otherUserId = userRepository.findByEmailIgnoreCase("noah@example.com").orElseThrow().getId();
		when(problemService.record(eq(problemId), any(), any(AuthenticatedUser.class), eq(false)))
				.thenReturn(new ProblemAttemptResponse(UUID.randomUUID(), UUID.randomUUID(), problemId,
						AttemptStatus.ATTEMPTED, 90, 2, null, Instant.now(), Instant.now()));

		mockMvc.perform(post("/api/problems/{id}/attempt", problemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":90,\"attempts\":2,\"userId\":\"" + otherUserId + "\"}")
				.header("Authorization", "Bearer " + ownerToken))
				.andExpect(status().isCreated());

		org.mockito.ArgumentCaptor<AuthenticatedUser> principal = org.mockito.ArgumentCaptor.forClass(AuthenticatedUser.class);
		verify(problemService).record(eq(problemId), any(), principal.capture(), eq(false));
		org.junit.jupiter.api.Assertions.assertNotEquals(otherUserId, principal.getValue().getId());
	}

	@Test
	void rejectsInvalidAttemptAndOnlyReturnsCallersProgress() throws Exception {
		String token = register(FIRST_USER);
		mockMvc.perform(post("/api/problems/{id}/solve", problemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":-1,\"attempts\":0}")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest());

		when(problemService.progress(any(AuthenticatedUser.class)))
				.thenReturn(new UserProgressResponse(28, 1, 3, List.of()));
		mockMvc.perform(get("/api/users/me/progress").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalProblems").value(28));
		verify(problemService).progress(any(AuthenticatedUser.class));
	}

	@Test
	void recordsSolvedStatusAndEnforcesMaximumPageSize() throws Exception {
		String token = register(FIRST_USER);
		when(problemService.record(eq(problemId), any(), any(AuthenticatedUser.class), eq(true)))
				.thenReturn(new ProblemAttemptResponse(UUID.randomUUID(), UUID.randomUUID(), problemId,
						AttemptStatus.SOLVED, 900, 2, Instant.now(), Instant.now(), Instant.now()));
		mockMvc.perform(post("/api/problems/{id}/solve", problemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":900,\"attempts\":2}")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("SOLVED"));

		mockMvc.perform(get("/api/problems").param("size", "51")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest());
	}

	@Test
	void recommendationsRequireJwtAndReturnExplanationAndScore() throws Exception {
		mockMvc.perform(get("/api/recommendations")).andExpect(status().isUnauthorized());
		String token = register(FIRST_USER);
		var recommendation = new RecommendationResponse(
				new ProblemListItemResponse(problemId, "Search The Ridge", "Find a target in a ridge-shaped list.",
						Difficulty.INTERMEDIATE, topicId, "Binary Search", List.of("binary search"), 25, false),
				"Recommended because your accuracy in Binary Search is 48% and you recently struggled with Medium problems.",
				BigDecimal.valueOf(72.50), "Binary Search", Difficulty.INTERMEDIATE);
		when(recommendationService.recommend(any(AuthenticatedUser.class))).thenReturn(List.of(recommendation));

		mockMvc.perform(get("/api/recommendations").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].problem.title").value("Search The Ridge"))
				.andExpect(jsonPath("$[0].reasonForRecommendation").value(
						"Recommended because your accuracy in Binary Search is 48% and you recently struggled with Medium problems."))
				.andExpect(jsonPath("$[0].recommendationScore").value(72.5))
				.andExpect(jsonPath("$[0].topic").value("Binary Search"))
				.andExpect(jsonPath("$[0].difficulty").value("INTERMEDIATE"));
	}

	@Test
	void roadmapRequiresJwtAndReturnsBackendComputedTopicState() throws Exception {
		mockMvc.perform(get("/api/users/me/roadmap")).andExpect(status().isUnauthorized());
		String token = register(FIRST_USER);
		var topic = new RoadmapTopicResponse(topicId, "Arrays", Difficulty.BEGINNER, List.of(),
				0, 20, 3, 4, BigDecimal.valueOf(75.00), 15, RoadmapTopicStatus.IN_PROGRESS,
				true, 1, BigDecimal.valueOf(75.00), "Continue strengthening this topic.");
		when(roadmapService.getRoadmap(any(AuthenticatedUser.class)))
				.thenReturn(new LearningRoadmapResponse(List.of(topic), List.of(topicId)));

		mockMvc.perform(get("/api/users/me/roadmap").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.topics[0].topicName").value("Arrays"))
				.andExpect(jsonPath("$.topics[0].status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.topics[0].progressPercent").value(15))
				.andExpect(jsonPath("$.topics[0].recommended").value(true))
				.andExpect(jsonPath("$.topics[0].prerequisites").isArray());
	}

	private String register(String body) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated()).andReturn();
		return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
	}

}