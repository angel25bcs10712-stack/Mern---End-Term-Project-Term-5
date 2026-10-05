package com.coderoute;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.coderoute.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:auth-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=integration-test-secret-that-is-more-than-32-bytes-long"
})
@AutoConfigureMockMvc
class AuthIntegrationTests {
	private static final String REGISTER = """
			{"name":"Ada Lovelace","email":"ada@example.com","password":"RoutePass123"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void clearUsers() {
		userRepository.deleteAll();
	}

	@Test
	void registersWithHashedPasswordAndReturnsJwt() throws Exception {
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.user.email").value("ada@example.com"))
				.andExpect(jsonPath("$.user.role").value("USER"))
				.andExpect(jsonPath("$.user.passwordHash").doesNotExist());
		var saved = userRepository.findByEmailIgnoreCase("ada@example.com").orElseThrow();
		org.junit.jupiter.api.Assertions.assertNotEquals("RoutePass123", saved.getPasswordHash());
		org.junit.jupiter.api.Assertions.assertTrue(saved.getPasswordHash().startsWith("$2a$"));
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		register();
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
				.andExpect(status().isConflict());
	}

	@Test
	void acceptsCorrectPasswordAndRejectsWrongPassword() throws Exception {
		register();
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ada@example.com\",\"password\":\"RoutePass123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ada@example.com\",\"password\":\"WrongPass123\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void requiresJwtForPrivateAndAdminEndpoints() throws Exception {
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/admin/example")).andExpect(status().isUnauthorized());

		MvcResult result = register();
		String token = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("ada@example.com"));
		mockMvc.perform(get("/api/admin/example").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void validatesRegistrationFields() throws Exception {
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ada\",\"email\":\"not-an-email\",\"password\":\"short\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void allowsRegistrationPreflightFromAlternateLocalVitePort() throws Exception {
		mockMvc.perform(options("/api/auth/register")
				.header("Origin", "http://127.0.0.1:5174")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5174"));
	}

	private MvcResult register() throws Exception {
		return mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
				.andExpect(status().isCreated())
				.andReturn();
	}
}