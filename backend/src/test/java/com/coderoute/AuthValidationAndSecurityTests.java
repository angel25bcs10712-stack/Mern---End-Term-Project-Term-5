package com.coderoute;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.coderoute.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:auth-reliability-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=auth-reliability-test-secret-that-is-at-least-32-bytes"
})
@AutoConfigureMockMvc
class AuthValidationAndSecurityTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("Invalid login: wrong password returns 401 Unauthorized")
	void invalidLogin_wrongPassword_returnsUnauthorized() throws Exception {
		registerUser("Grace Hopper", "grace@example.com", "SecretPass123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"grace@example.com\",\"password\":\"WrongPassword!\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Email or password is incorrect"));
	}

	@Test
	@DisplayName("Invalid login: non-existent email returns 401 Unauthorized")
	void invalidLogin_nonExistentEmail_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ghost@example.com\",\"password\":\"SecretPass123\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Email or password is incorrect"));
	}

	@Test
	@DisplayName("Invalid login: blank email or malformed email returns 400 Bad Request")
	void invalidLogin_malformedEmail_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"not-an-email\",\"password\":\"SecretPass123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"\",\"password\":\"SecretPass123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Invalid login: password too short (< 8 chars) returns 400 Bad Request")
	void invalidLogin_shortPassword_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"user@example.com\",\"password\":\"short\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Invalid login: unreadable request body returns 400 Bad Request")
	void invalidLogin_malformedJson_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{invalid-json}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Request body is invalid"));
	}

	@Test
	@DisplayName("Duplicate registration: identical email returns 409 Conflict")
	void duplicateRegistration_exactEmail_returnsConflict() throws Exception {
		registerUser("Katherine Johnson", "katherine@example.com", "OrbitalPass123");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Katherine J.\",\"email\":\"katherine@example.com\",\"password\":\"NewPass123\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value("An account with this email already exists"));
	}

	@Test
	@DisplayName("Duplicate registration: case-insensitive email match returns 409 Conflict")
	void duplicateRegistration_caseInsensitiveEmail_returnsConflict() throws Exception {
		registerUser("Katherine Johnson", "katherine@example.com", "OrbitalPass123");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Katherine J.\",\"email\":\"KATHERINE@EXAMPLE.COM\",\"password\":\"NewPass123\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	@DisplayName("Validation tests: register with empty name or blank fields returns 400 Bad Request")
	void registerValidation_blankFields_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"email\":\"test@example.com\",\"password\":\"ValidPass123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("Unauthorized API request: missing Authorization header returns 401 Unauthorized")
	void unauthorizedApiRequest_missingToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/problems"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/recommendations"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Unauthorized API request: invalid or malformed JWT returns 401 Unauthorized")
	void unauthorizedApiRequest_invalidToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/auth/me")
				.header("Authorization", "Bearer invalid.fake.token"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Unauthorized resource access: standard user accessing /api/admin/** returns 403 Forbidden")
	void unauthorizedResourceAccess_userRoleAccessingAdmin_returnsForbidden() throws Exception {
		String token = registerUser("Alan Turing", "alan@example.com", "EnigmaPass123");

		mockMvc.perform(get("/api/admin/stats")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/problems")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
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
