package com.coderoute.leetcode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.coderoute.dto.leetcode.LeetCodeStatsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fetches basic public LeetCode stats through LeetCode's public GraphQL API.
 * Only the public profile name (derived from the stored profile URL) is used;
 * no LeetCode credentials are requested or stored.
 */
@Service
public class LeetCodeService {
	private static final String GRAPHQL_ENDPOINT = "https://leetcode.com/graphql";
	private static final String STATS_QUERY = """
			query userProblemsSolved($username: String!) {
			  matchedUser(username: $username) {
			    submitStatsGlobal {
			      acSubmissionNum {
			        difficulty
			        count
			      }
			    }
			  }
			  userContestRanking(username: $username) {
			    rating
			  }
			}""";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpClient httpClient;

	public LeetCodeService() {
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(6))
				.build();
	}

	public LeetCodeStatsResponse stats(String profileUrl) {
		String username = extractUsername(profileUrl);
		String requestBody;
		try {
			requestBody = objectMapper.writeValueAsString(Map.of(
					"query", STATS_QUERY,
					"variables", Map.of("username", username)));
		} catch (IOException exception) {
			throw new LeetCodeUnavailableException("LeetCode stats are temporarily unavailable");
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(GRAPHQL_ENDPOINT))
				.timeout(Duration.ofSeconds(8))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.header("User-Agent", "Mozilla/5.0 (compatible; CodeRoute/1.0)")
				.header("Referer", "https://leetcode.com/")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new LeetCodeUnavailableException("Could not reach LeetCode right now. Please try again later.");
		}
		if (response.statusCode() != 200) {
			throw new LeetCodeUnavailableException("Could not reach LeetCode right now. Please try again later.");
		}
		return parseStats(username, response.body());
	}

	private LeetCodeStatsResponse parseStats(String username, String body) {
		JsonNode data;
		try {
			JsonNode root = objectMapper.readTree(body);
			if (root.path("errors").isArray() && !root.path("errors").isEmpty()) {
				throw new LeetCodeUnavailableException("LeetCode stats are temporarily unavailable");
			}
			data = root.path("data");
		} catch (IOException exception) {
			throw new LeetCodeUnavailableException("LeetCode stats are temporarily unavailable");
		}

		JsonNode matchedUser = data.path("matchedUser");
		if (matchedUser.isMissingNode() || matchedUser.isNull()) {
			throw new LeetCodeUnavailableException("LeetCode profile not found. Check the profile link and try again.");
		}


		int total = 0;
		int easy = 0;
		int medium = 0;
		int hard = 0;
		for (JsonNode entry : matchedUser.path("submitStatsGlobal").path("acSubmissionNum")) {
			int count = entry.path("count").asInt(0);
			switch (entry.path("difficulty").asText("")) {
				case "All" -> total = count;
				case "Easy" -> easy = count;
				case "Medium" -> medium = count;
				case "Hard" -> hard = count;
				default -> {
				}
			}
		}

		JsonNode ranking = data.path("userContestRanking");
		Double contestRating = (ranking.isMissingNode() || ranking.isNull() || !ranking.has("rating"))
				? null
				: ranking.path("rating").asDouble();

		return new LeetCodeStatsResponse(username, total, easy, medium, hard, contestRating);
	}

	/**
	 * Accepts common profile link shapes such as
	 * https://leetcode.com/u/username/, https://leetcode.com/username/ or a bare name.
	 */
	String extractUsername(String profileUrl) {
		if (profileUrl == null || profileUrl.isBlank()) {
			throw new IllegalArgumentException("A LeetCode profile link is required");
		}
		String path = profileUrl.trim();
		int schemeIndex = path.indexOf("://");
		if (schemeIndex >= 0) {
			path = path.substring(schemeIndex + 3);
		}
		int queryIndex = path.indexOf('?');
		if (queryIndex >= 0) {
			path = path.substring(0, queryIndex);
		}
		int fragmentIndex = path.indexOf('#');
		if (fragmentIndex >= 0) {
			path = path.substring(0, fragmentIndex);
		}
		int slashIndex = path.indexOf('/');
		String firstSegment = slashIndex >= 0 ? path.substring(0, slashIndex) : path;
		String remainder = slashIndex >= 0 ? path.substring(slashIndex + 1) : "";

		List<String> reservedSegments = List.of("u", "users");
		String username;
		String host = firstSegment.toLowerCase(Locale.ROOT);
		if (host.equals("leetcode.com") || host.equals("www.leetcode.com")) {
			int nextSlash = remainder.indexOf('/');
			String segment = nextSlash >= 0 ? remainder.substring(0, nextSlash) : remainder;
			if (reservedSegments.contains(segment.toLowerCase(Locale.ROOT))) {
				String afterReserved = nextSlash >= 0 ? remainder.substring(nextSlash + 1) : "";
				int nameSlash = afterReserved.indexOf('/');
				username = nameSlash >= 0 ? afterReserved.substring(0, nameSlash) : afterReserved;
			} else {
				username = segment;
			}
		} else {
			username = firstSegment;
		}

		username = username.trim();
		if (username.isEmpty() || !username.matches("[A-Za-z0-9_-]{1,50}")) {
			throw new IllegalArgumentException(
					"Enter a valid LeetCode profile link, for example https://leetcode.com/u/username");
		}
		return username;
	}
}
