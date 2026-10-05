package com.coderoute;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.coderoute.admin.AdminService;
import com.coderoute.dto.admin.AdminProblemResponse;
import com.coderoute.dto.admin.AdminStatsResponse;
import com.coderoute.dto.admin.AdminTopicResponse;
import com.coderoute.entity.User;
import com.coderoute.entity.enums.UserRole;
import com.coderoute.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:admin-api-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=admin-api-test-secret-that-is-more-than-32-bytes"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class AdminControllerTests {
	private static final String PASSWORD = "SecureAdminPass123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private AdminService adminService;

	@BeforeEach
	void clearUsers() {
		userRepository.deleteAll();
	}

	@Test
	void adminCanManageCatalogAndRegularUsersAreForbidden() throws Exception {
		mockMvc.perform(get("/api/admin/stats")).andExpect(status().isUnauthorized());
		String regularToken = registerRegularUser();
		mockMvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + regularToken))
				.andExpect(status().isForbidden());

		userRepository.save(new User("Admin", "admin@example.com", passwordEncoder.encode(PASSWORD), UserRole.ADMIN));
		String adminToken = loginAdmin();
		String authorization = "Bearer " + adminToken;
		org.mockito.Mockito.when(adminService.stats()).thenReturn(new AdminStatsResponse(2, 2, 1, 0, 0));
		org.mockito.Mockito.when(adminService.createTopic(org.mockito.ArgumentMatchers.any()))
				.thenReturn(new AdminTopicResponse(java.util.UUID.randomUUID(), "Arrays", null,
						com.coderoute.entity.enums.Difficulty.BEGINNER, null, List.of()));
		org.mockito.Mockito.when(adminService.createProblem(org.mockito.ArgumentMatchers.any()))
				.thenReturn(new AdminProblemResponse(java.util.UUID.randomUUID(), "Find the pair", "Find two values.",
						com.coderoute.entity.enums.Difficulty.BEGINNER, java.util.UUID.randomUUID(), "Arrays", null,
						List.of("arrays"), 20));
		org.mockito.Mockito.when(adminService.updatePrerequisites(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any()))
				.thenReturn(new AdminTopicResponse(java.util.UUID.randomUUID(), "Graphs", null,
						com.coderoute.entity.enums.Difficulty.ADVANCED, null, List.of()));

		mockMvc.perform(post("/api/admin/topics")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Arrays\",\"difficulty\":\"BEGINNER\"}")
				.header("Authorization", authorization))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Arrays"));
		java.util.UUID topicId = java.util.UUID.randomUUID();

		mockMvc.perform(put("/api/admin/topics/{id}/prerequisites", topicId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"prerequisiteTopicIds\":[]}")
				.header("Authorization", authorization))
				.andExpect(status().isOk());

		String problemJson = "{\"title\":\"Find the pair\",\"description\":\"Find two values that sum to a target.\","
				+ "\"difficulty\":\"BEGINNER\",\"topicId\":\"" + topicId + "\",\"externalUrl\":null,"
				+ "\"tags\":[\"arrays\",\"hashing\"],\"estimatedTimeMinutes\":20}";
		mockMvc.perform(post("/api/admin/problems")
				.contentType(MediaType.APPLICATION_JSON).content(problemJson).header("Authorization", authorization))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Find the pair"));

		mockMvc.perform(get("/api/admin/stats").header("Authorization", authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.users").value(2))
				.andExpect(jsonPath("$.topics").value(2))
				.andExpect(jsonPath("$.problems").value(1))
				.andExpect(jsonPath("$.userEmails").doesNotExist());

		mockMvc.perform(delete("/api/admin/problems/{id}", topicId).header("Authorization", authorization))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/admin/topics/{id}", topicId).header("Authorization", authorization))
				.andExpect(status().isNoContent());
	}

	@Test
	void rejectsInvalidAdminCatalogInput() throws Exception {
		userRepository.save(new User("Admin", "admin@example.com", passwordEncoder.encode(PASSWORD), UserRole.ADMIN));
		String token = loginAdmin();
		mockMvc.perform(post("/api/admin/problems")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\" \",\"description\":\"\",\"difficulty\":\"UNKNOWN\",\"tags\":[]}")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest());
	}

	private String registerRegularUser() throws Exception {
		String body = "{\"name\":\"Learner\",\"email\":\"learner@example.com\",\"password\":\"LearnerPass123\"}";
		String response = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(response, "$.token");
	}

	private String loginAdmin() throws Exception {
		String body = "{\"email\":\"admin@example.com\",\"password\":\"" + PASSWORD + "\"}";
		String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(response, "$.token");
	}
}