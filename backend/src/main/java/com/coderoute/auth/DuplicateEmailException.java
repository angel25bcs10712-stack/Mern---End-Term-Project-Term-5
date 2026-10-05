package com.coderoute.auth;

public class DuplicateEmailException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DuplicateEmailException() {
		super("An account with this email already exists");
	}
}