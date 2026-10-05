package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.coderoute.entity.StringArrayJsonConverter;

class StringArrayJsonConverterTests {

	private final StringArrayJsonConverter converter = new StringArrayJsonConverter();

	@Test
	void readsJsonTextFormat() {
		assertArrayEquals(new String[] { "prefix sums", "range queries" },
				converter.convertToEntityAttribute("[\"prefix sums\", \"range queries\"]"));
	}

	@Test
	void readsPostgresArrayLiteralFormat() {
		assertArrayEquals(new String[] { "prefix sums", "range queries" },
				converter.convertToEntityAttribute("{\"prefix sums\",\"range queries\"}"));
	}

	@Test
	void readsUnquotedPostgresArrayLiteral() {
		assertArrayEquals(new String[] { "in-place", "rotation" },
				converter.convertToEntityAttribute("{in-place,rotation}"));
	}

	@Test
	void readsEmptyPostgresArrayLiteral() {
		assertArrayEquals(new String[0], converter.convertToEntityAttribute("{}"));
	}

	@Test
	void readsEmptyJsonArray() {
		assertArrayEquals(new String[0], converter.convertToEntityAttribute("[]"));
	}

	@Test
	void writeThenReadRoundTrip() {
		String[] tags = new String[] { "arrays", "two pointers" };
		assertArrayEquals(tags, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(tags)));
	}

	@Test
	void rejectsUnparseableValue() {
		assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("not-an-array"));
	}
}
