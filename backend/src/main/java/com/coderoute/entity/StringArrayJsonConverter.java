package com.coderoute.entity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringArrayJsonConverter implements AttributeConverter<String[], String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String EMPTY_ARRAY_JSON = "[]";

	@Override
	public String convertToDatabaseColumn(String[] attribute) {
		if (attribute == null) {
			return EMPTY_ARRAY_JSON;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Failed to serialize tags array", exception);
		}
	}

	@Override
	public String[] convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return new String[0];
		}
		String trimmed = dbData.trim();
		if (EMPTY_ARRAY_JSON.equals(trimmed) || "{}".equals(trimmed)) {
			return new String[0];
		}
		if (trimmed.startsWith("[")) {
			try {
				String[] value = OBJECT_MAPPER.readValue(trimmed, String[].class);
				return value == null ? new String[0] : value;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to deserialize tags array", exception);
			}
		}
		if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
			return parsePostgresArrayLiteral(trimmed);
		}
		throw new IllegalArgumentException("Failed to deserialize tags array: unsupported format");
	}

	private static String[] parsePostgresArrayLiteral(String literal) {
		String inner = literal.substring(1, literal.length() - 1);
		if (inner.isBlank()) {
			return new String[0];
		}
		java.util.List<String> elements = new java.util.ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		boolean quoted = false;
		boolean escaped = false;
		boolean hasContent = false;
		for (int i = 0; i < inner.length(); i++) {
			char c = inner.charAt(i);
			if (escaped) {
				current.append(c);
				escaped = false;
				hasContent = true;
				continue;
			}
			if (c == '\\' && inQuotes) {
				escaped = true;
				hasContent = true;
				continue;
			}
			if (c == '"') {
				inQuotes = !inQuotes;
				quoted = true;
				hasContent = true;
				continue;
			}
			if (c == ',' && !inQuotes) {
				elements.add(finishElement(current.toString(), quoted, hasContent));
				current.setLength(0);
				quoted = false;
				hasContent = false;
				continue;
			}
			current.append(c);
			hasContent = true;
		}
		if (inQuotes || escaped) {
			throw new IllegalArgumentException("Failed to deserialize tags array: malformed array literal");
		}
		elements.add(finishElement(current.toString(), quoted, hasContent));
		return elements.toArray(String[]::new);
	}

	private static String finishElement(String raw, boolean quoted, boolean hasContent) {
		if (!hasContent) {
			return "";
		}
		if (quoted) {
			return raw;
		}
		String trimmed = raw.trim();
		if ("NULL".equals(trimmed)) {
			return null;
		}
		return trimmed;
	}
}
