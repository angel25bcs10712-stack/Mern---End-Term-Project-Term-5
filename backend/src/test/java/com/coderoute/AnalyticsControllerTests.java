package com.coderoute;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.coderoute.analytics.AnalyticsService;
import com.coderoute.dto.analytics.PerformanceAnalyticsResponse;
import com.coderoute.dto.analytics.TrendComparison;
import com.coderoute.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:analytics-api-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=analytics-api-test-secret-that-is-more-than-32-bytes"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class AnalyticsControllerTests {
	private static final String REGISTER = """
			{"name":"Sam Learner","email":"sam.analytics@example.com","password":"AnalyticsPass123"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private AnalyticsService analyticsService;

	@BeforeEach
	void clearUsers() {
		userRepository.deleteAll();
	}

	@Test
	void analyticsRequiresJwtAndUsesOnlyTheAuthenticatedUser() throws Exception {
		mockMvc.perform(get("/api/analytics/me/performance")).andExpect(status().isUnauthorized());
		MvcResult registration = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON).content(REGISTER))
				.andExpect(status().isCreated()).andReturn();
		String token = com.jayway.jsonpath.JsonPath.read(registration.getResponse().getContentAsString(), "$.token");
		UUID userId = userRepository.findByEmailIgnoreCase("sam.analytics@example.com").orElseThrow().getId();
		var response = new PerformanceAnalyticsResponse(12, 8, 66.67, 510.0, 43.75,
				List.of(), List.of(), List.of(), new TrendComparison(50.0, 75.0, 25.0, "IMPROVING", 4, 4), List.of());
		when(analyticsService.performance(userId)).thenReturn(response);

		mockMvc.perform(get("/api/analytics/me/performance").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.overallAccuracy").value(66.67))
				.andExpect(jsonPath("$.improvementTrend.direction").value("IMPROVING"))
				.andExpect(jsonPath("$.userEmail").doesNotExist());
		verify(analyticsService).performance(userId);
	}
}