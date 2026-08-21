package com.app.dating;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Boots the full Spring context (Flyway migrations included) against a real
 * Postgres+PostGIS container, so subclasses exercise the actual schema and native
 * queries rather than mocks. Requires Docker to be available on the host running
 * the build; GitHub Actions' ubuntu runners have it, so backend-ci.yml runs these
 * for real even in environments where a local `./gradlew test` cannot.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
		DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
	);

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

}
