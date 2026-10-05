package com.coderoute.admin;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coderoute.dto.error.ApiError;

@RestControllerAdvice(assignableTypes = AdminController.class)
public class AdminExceptionHandler {
	@ExceptionHandler(AdminInputException.class)
	public ResponseEntity<ApiError> invalidAdminInput(AdminInputException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(AdminConflictException.class)
	public ResponseEntity<ApiError> adminConflict(AdminConflictException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage());
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message));
	}
}