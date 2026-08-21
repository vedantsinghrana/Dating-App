package com.app.dating.auth;

import com.app.dating.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFlowIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void registerThenLoginThenCallProtectedEndpointWithIssuedToken() {
		Map<String, String> registerBody = Map.of("email", "flow@example.com", "password", "correct-horse-battery");
		ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity("/api/auth/register", registerBody, AuthResponse.class);
		assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(registerResponse.getBody()).isNotNull();
		assertThat(registerResponse.getBody().token()).isNotBlank();

		Map<String, String> loginBody = Map.of("email", "flow@example.com", "password", "correct-horse-battery");
		ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginBody, AuthResponse.class);
		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(loginResponse.getBody()).isNotNull();
		String token = loginResponse.getBody().token();
		assertThat(token).isNotBlank();

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		ResponseEntity<String> profileResponse = restTemplate.exchange(
			"/api/profiles/me", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), String.class
		);
		// No profile created yet, but reaching a 404 (not a 401) proves the JWT was accepted.
		assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void loginWithWrongPasswordIsRejected() {
		restTemplate.postForEntity("/api/auth/register", Map.of("email", "wrongpass@example.com", "password", "correct-password"), AuthResponse.class);

		ResponseEntity<String> response = restTemplate.postForEntity(
			"/api/auth/login", Map.of("email", "wrongpass@example.com", "password", "incorrect-password"), String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void registeringDuplicateEmailIsRejected() {
		Map<String, String> body = Map.of("email", "dupe@example.com", "password", "correct-password");
		restTemplate.postForEntity("/api/auth/register", body, AuthResponse.class);

		ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", body, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

}
