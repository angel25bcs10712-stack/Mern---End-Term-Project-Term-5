package com.coderoute.leetcode;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coderoute.dto.error.ApiError;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = LeetCodeController.class)
public class LeetCodeExceptionHandler {
	@ExceptionHandler(LeetCodeUnavailableException.class)
	public ResponseEntity<ApiError> leetCodeUnavailable(LeetCodeUnavailableException exception) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiError> missingParameter(MissingServletRequestParameterException exception) {
		return error(HttpStatus.BAD_REQUEST,
				"Request parameter '" + exception.getParameterName() + "' is required");
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message));
	}
}
