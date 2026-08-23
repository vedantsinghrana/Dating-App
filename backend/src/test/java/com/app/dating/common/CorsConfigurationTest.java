package com.app.dating.common;

import com.app.dating.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Without this, the web client (a different origin/port from the API) can't call it at
 * all — the browser blocks the request before it ever reaches a controller.
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class CorsConfigurationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtService jwtService;

	@Test
	void allowsConfiguredWebOrigin() throws Exception {
		mockMvc.perform(get("/api/health").header(HttpHeaders.ORIGIN, "http://localhost:5180"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5180"));
	}

	@Test
	void rejectsUnknownOrigin() throws Exception {
		mockMvc.perform(get("/api/health").header(HttpHeaders.ORIGIN, "https://evil.example.com"))
			.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}

}
