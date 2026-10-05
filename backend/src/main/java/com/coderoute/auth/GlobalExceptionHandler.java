package com.coderoute.auth;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.coderoute.dto.error.ApiError;
import com.coderoute.error.ResourceNotFoundException;
import com.coderoute.analytics.AnalyticsUnavailableException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiError> duplicateEmail(DuplicateEmailException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> resourceNotFound(ResourceNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> noResourceFound() {
		return error(HttpStatus.NOT_FOUND, "Requested endpoint was not found");
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> badCredentials() {
		return error(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> accessDenied() {
		return error(HttpStatus.FORBIDDEN, "Access is denied");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> invalidRequest(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(field -> field.getField() + ": " + field.getDefaultMessage())
				.collect(Collectors.joining(", "));
		return error(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> argumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		return error(HttpStatus.BAD_REQUEST, "Invalid parameter: " + exception.getName());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> unreadableRequest() {
		return error(HttpStatus.BAD_REQUEST, "Request body is invalid");
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> constraintViolation() {
		return error(HttpStatus.BAD_REQUEST, "One or more request parameters are invalid");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> illegalArgument(IllegalArgumentException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(AnalyticsUnavailableException.class)
	public ResponseEntity<ApiError> analyticsUnavailable(AnalyticsUnavailableException exception) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiError> databaseFailure(DataAccessException exception) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, "Database service is temporarily unavailable");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> unexpectedError() {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message));
	}
}