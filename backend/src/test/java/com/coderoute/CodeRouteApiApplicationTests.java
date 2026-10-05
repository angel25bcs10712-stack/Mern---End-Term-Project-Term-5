package com.coderoute;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
		"spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
		"app.jwt.secret=context-test-secret-that-is-more-than-32-bytes-long"
})
class CodeRouteApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
