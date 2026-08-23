package com.app.dating;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Boots the full Spring context (Flyway migrations included) against a real
 * Postgres+PostGIS container, so subclasses exercise the actual schema and native
 * queries rather than mocks. Requires Docker to be available on the host running
 * the build; GitHub Actions' ubuntu runners have it, so backend-ci.yml runs these
 * for real even in environments where a local `./gradlew test` cannot.
 *
 * The container is started once in a static initializer and deliberately never stopped
 * here — this is Testcontainers' documented "singleton container" pattern for sharing one
 * instance across multiple test classes. Using @Testcontainers/@Container instead makes
 * *each* subclass's own JUnit lifecycle stop the (shared, static) container in its
 * afterAll, killing it for whichever test class happens to run next; Ryuk reaps it when
 * the JVM exits regardless.
 *
 * Scheduled jobs are disabled here: MatchExpiryJob in particular uses fixedRate with no
 * initialDelay, so in a real (non-mocked) Spring context it fires almost immediately on
 * startup and would race against test-managed match data.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
	"app.match.expiry-job.enabled=false",
	"app.top-pick.job.enabled=false"
})
public abstract class AbstractIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
		DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
	);

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

}
