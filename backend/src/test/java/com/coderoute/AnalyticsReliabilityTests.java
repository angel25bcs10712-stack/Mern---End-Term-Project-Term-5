package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
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
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.coderoute.analytics.AnalyticsService;
import com.coderoute.analytics.AnalyticsUnavailableException;
import com.coderoute.repository.ProblemAttemptRepository;
import com.coderoute.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:analytics-reliability-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=analytics-reliability-secret-that-is-at-least-32-bytes"
})
@AutoConfigureMockMvc
class AnalyticsReliabilityTests {

	private static final String REGISTER = """
			{"name":"Alex Rivers","email":"alex.analytics@example.com","password":"AnalyticsPass123"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProblemAttemptRepository attemptRepository;

	@MockitoBean
	private AnalyticsService analyticsService;

	private String authToken;
	private UUID userId;

	@BeforeEach
	void setUp() throws Exception {
		userRepository.deleteAll();
		MvcResult registration = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON).content(REGISTER))
				.andExpect(status().isCreated()).andReturn();
		authToken = com.jayway.jsonpath.JsonPath.read(registration.getResponse().getContentAsString(), "$.token");
		userId = userRepository.findByEmailIgnoreCase("alex.analytics@example.com").orElseThrow().getId();
	}

	@Test
	@DisplayName("Analytics service unavailable: performance endpoint returns 503 Service Unavailable")
	void analyticsUnavailable_performance_returnsServiceUnavailable() throws Exception {
		when(analyticsService.performance(userId)).thenThrow(new AnalyticsUnavailableException());

		mockMvc.perform(get("/api/analytics/me/performance")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.message").value("Performance analytics is temporarily unavailable."));
	}

	@Test
	@DisplayName("Analytics service unavailable: weak-topics endpoint returns 503 Service Unavailable")
	void analyticsUnavailable_weakTopics_returnsServiceUnavailable() throws Exception {
		when(analyticsService.weakTopics(userId)).thenThrow(new AnalyticsUnavailableException());

		mockMvc.perform(get("/api/analytics/me/weak-topics")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.message").value("Performance analytics is temporarily unavailable."));
	}

	@Test
	@DisplayName("Analytics service unavailable: trend endpoint returns 503 Service Unavailable")
	void analyticsUnavailable_trend_returnsServiceUnavailable() throws Exception {
		when(analyticsService.trend(userId)).thenThrow(new AnalyticsUnavailableException());

		mockMvc.perform(get("/api/analytics/me/trend")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.message").value("Performance analytics is temporarily unavailable."));
	}

	@Test
	@DisplayName("Analytics service unit test: missing internal token throws AnalyticsUnavailableException")
	void analyticsService_missingToken_throwsAnalyticsUnavailableException() {
		AnalyticsService serviceWithoutToken = new AnalyticsService(
				attemptRepository,
				"http://127.0.0.1:8000",
				"" // empty token
		);

		assertThrows(AnalyticsUnavailableException.class, () -> {
			serviceWithoutToken.performance(userId);
		});
	}

	@Test
	@DisplayName("Analytics service unit test: unreachable host throws AnalyticsUnavailableException")
	void analyticsService_unreachableHost_throwsAnalyticsUnavailableException() {
		AnalyticsService serviceUnreachableHost = new AnalyticsService(
				attemptRepository,
				"http://127.0.0.1:54321", // port with no listener
				"valid-test-token"
		);

		assertThrows(AnalyticsUnavailableException.class, () -> {
			serviceUnreachableHost.performance(userId);
		});
	}

	@Test
	@DisplayName("Database/API failure: DataAccessException mapped to 503 Service Unavailable")
	void databaseFailure_returnsServiceUnavailable() throws Exception {
		when(analyticsService.performance(userId))
				.thenThrow(new DataAccessResourceFailureException("Simulated connection timeout to database"));

		mockMvc.perform(get("/api/analytics/me/performance")
				.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.message").value("Database service is temporarily unavailable"));
	}
}
