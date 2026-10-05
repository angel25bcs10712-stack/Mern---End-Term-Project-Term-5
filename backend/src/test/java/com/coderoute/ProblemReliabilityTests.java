package com.coderoute;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.coderoute.dto.attempt.ProblemAttemptRequest;
import com.coderoute.dto.problem.ProblemDetailResponse;
import com.coderoute.entity.Problem;
import com.coderoute.entity.Topic;
import com.coderoute.entity.User;
import com.coderoute.entity.enums.Difficulty;
import com.coderoute.entity.enums.UserRole;
import com.coderoute.error.ResourceNotFoundException;
import com.coderoute.problem.ProblemService;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.ProblemRepository;
import com.coderoute.repository.TopicRepository;
import com.coderoute.repository.UserRepository;
import com.coderoute.repository.UserTopicProgressRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:problem-reliability-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=problem-reliability-test-secret-that-is-at-least-32-bytes"
})
@AutoConfigureMockMvc
class ProblemReliabilityTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProblemRepository problemRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private ProblemAttemptRepository attemptRepository;

	@Autowired
	private UserTopicProgressRepository progressRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProblemService problemService;

	private Topic testTopic;
	private Problem testProblem;
	private String authToken;
	private User authUser;

	@BeforeEach
	void setUp() throws Exception {
		progressRepository.deleteAll();
		attemptRepository.deleteAll();
		problemRepository.deleteAll();
		topicRepository.deleteAll();
		userRepository.deleteAll();

		testTopic = topicRepository.save(new Topic("Arrays", "Array manipulation", Difficulty.BEGINNER, null));
		testProblem = problemRepository.save(new Problem(
				"Two Sum",
				"Given an array of integers nums and an integer target...",
				Difficulty.BEGINNER,
				testTopic,
				"https://leetcode.com",
				new String[] { "arrays", "hash-table" },
				15
		));

		authToken = registerUser("Dev Candidate", "dev@example.com", "Password123");
		authUser = userRepository.findByEmailIgnoreCase("dev@example.com").orElseThrow();
	}

	@Test
	@DisplayName("Invalid problem ID: malformed UUID in path returns 400 Bad Request")
	void invalidProblemId_malformedUuid_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/problems/not-a-valid-uuid")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Invalid parameter: id"));

		mockMvc.perform(post("/api/problems/12345/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":300,\"attempts\":1}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Invalid problem ID: non-existent UUID returns 404 Not Found")
	void invalidProblemId_nonExistentUuid_returnsNotFound() throws Exception {
		UUID randomId = UUID.randomUUID();

		mockMvc.perform(get("/api/problems/" + randomId)
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Problem not found"));

		mockMvc.perform(post("/api/problems/" + randomId + "/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":300,\"attempts\":1}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Problem not found"));
	}

	@Test
	@DisplayName("Invalid attempt data: negative timeTakenSeconds returns 400 Bad Request")
	void invalidAttemptData_negativeTime_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/attempt")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":-10,\"attempts\":1}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Invalid attempt data: zero or negative attempts returns 400 Bad Request")
	void invalidAttemptData_zeroOrNegativeAttempts_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":300,\"attempts\":0}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/attempt")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":300,\"attempts\":-3}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Invalid attempt data: null or missing fields returns 400 Bad Request")
	void invalidAttemptData_nullFields_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":null,\"attempts\":1}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":120}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Duplicate solve operation: records attempt idempotently without inflating distinct solved count")
	void duplicateSolveOperation_preservesDistinctStatistics() throws Exception {
		// First solve
		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":600,\"attempts\":2}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("SOLVED"));

		// Check progress after 1st solve
		mockMvc.perform(get("/api/users/me/progress")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.problemsSolved").value(1))
				.andExpect(jsonPath("$.topics[0].problemsSolved").value(1));

		// Second solve of the exact same problem (e.g. practicing again or duplicate submit)
		mockMvc.perform(post("/api/problems/" + testProblem.getId() + "/solve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"timeTakenSeconds\":400,\"attempts\":1}")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("SOLVED"));

		// Check progress after 2nd solve: distinct solved problems MUST STILL BE 1
		mockMvc.perform(get("/api/users/me/progress")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.problemsSolved").value(1))
				.andExpect(jsonPath("$.topics[0].problemsSolved").value(1));

		// Problem detail must confirm solved = true
		mockMvc.perform(get("/api/problems/" + testProblem.getId())
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.solved").value(true));
	}

	@Test
	@DisplayName("Service test: get non-existent problem throws ResourceNotFoundException")
	void serviceTest_getNonExistentProblem_throwsException() {
		com.coderoute.auth.AuthenticatedUser principal = new com.coderoute.auth.AuthenticatedUser(authUser);
		org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			problemService.get(UUID.randomUUID(), principal);
		});
	}

	@Test
	@DisplayName("Service test: record attempt returns valid response and marks solved status")
	void serviceTest_recordAttempt_updatesUserProgress() {
		com.coderoute.auth.AuthenticatedUser principal = new com.coderoute.auth.AuthenticatedUser(authUser);
		var response = problemService.record(
				testProblem.getId(),
				new ProblemAttemptRequest(300, 1),
				principal,
				true
		);

		org.junit.jupiter.api.Assertions.assertEquals(testProblem.getId(), response.problemId());
		org.junit.jupiter.api.Assertions.assertEquals(com.coderoute.entity.enums.AttemptStatus.SOLVED, response.status());

		ProblemDetailResponse detail = problemService.get(testProblem.getId(), principal);
		org.junit.jupiter.api.Assertions.assertTrue(detail.solved());
	}

	private String registerUser(String name, String email, String password) throws Exception {
		String json = String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", name, email, password);
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isCreated())
				.andReturn();
		return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
	}
}
