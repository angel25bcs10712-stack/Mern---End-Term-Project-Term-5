package com.coderoute.analytics;

public class AnalyticsUnavailableException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AnalyticsUnavailableException() {
		super("Performance analytics is temporarily unavailable.");
	}
}