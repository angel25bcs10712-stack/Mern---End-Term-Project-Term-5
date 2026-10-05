package com.coderoute.admin;

public class AdminConflictException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AdminConflictException(String message) {
		super(message);
	}
}