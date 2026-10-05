package com.coderoute.analytics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.coderoute.dto.analytics.AnalyticsAttemptData;
import com.coderoute.dto.analytics.AnalyticsInput;
import com.coderoute.dto.analytics.PerformanceAnalyticsResponse;
import com.coderoute.dto.analytics.TrendAnalyticsResponse;
import com.coderoute.dto.analytics.WeakTopic;
import com.coderoute.repository.ProblemAttemptRepository;

@Service
public class AnalyticsService {
	private static final int MAX_ANALYTICS_ATTEMPTS = 2000;

	private final ProblemAttemptRepository attemptRepository;
	private final RestClient restClient;
	private final String internalToken;

	public AnalyticsService(ProblemAttemptRepository attemptRepository,
			@Value("${analytics.service.url:http://127.0.0.1:8000}") String serviceUrl,
			@Value("${analytics.service.token:}") String internalToken) {
		this.attemptRepository = attemptRepository;
		this.internalToken = internalToken;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(2));
		requestFactory.setReadTimeout(java.time.Duration.ofSeconds(8));
		this.restClient = RestClient.builder().baseUrl(serviceUrl).requestFactory(requestFactory).build();
	}

	public PerformanceAnalyticsResponse performance(UUID userId) {
		return request(userId, "performance", new ParameterizedTypeReference<>() {});
	}

	public List<WeakTopic> weakTopics(UUID userId) {
		return request(userId, "weak-topics", new ParameterizedTypeReference<>() {});
	}

	public TrendAnalyticsResponse trend(UUID userId) {
		return request(userId, "trend", new ParameterizedTypeReference<>() {});
	}

	private <T> T request(UUID userId, String endpoint, ParameterizedTypeReference<T> responseType) {
		if (internalToken == null || internalToken.isBlank()) {
			throw new AnalyticsUnavailableException();
		}
		AnalyticsInput input = loadInput(userId);
		try {
			T result = restClient.post()
					.uri("/analytics/user/{userId}/{endpoint}", userId, endpoint)
					.header("X-Internal-Token", internalToken)
					.body(input)
					.retrieve()
					.body(responseType);
			if (result == null) throw new AnalyticsUnavailableException();
			return result;
		} catch (RestClientException exception) {
			throw new AnalyticsUnavailableException();
		}
	}

	private AnalyticsInput loadInput(UUID userId) {
		List<AnalyticsAttemptData> attempts = new ArrayList<>(attemptRepository.findAnalyticsAttemptsByUserId(
				userId, PageRequest.of(0, MAX_ANALYTICS_ATTEMPTS, Sort.by(Sort.Direction.DESC, "createdAt"))));
		attempts.sort(Comparator.comparing(AnalyticsAttemptData::occurredAt));
		return new AnalyticsInput(attempts);
	}
}