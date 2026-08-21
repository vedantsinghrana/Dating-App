package com.app.dating.auth;

import com.app.dating.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, passwordEncoder, jwtService);
	}

	@Test
	void registerRejectsDuplicateEmail() {
		when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(new RegisterRequest("taken@example.com", "password123")))
			.isInstanceOf(ApiException.class)
			.hasMessage("Email already in use");
	}

	@Test
	void registerCreatesUserAndReturnsToken() {
		when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		// GenerationType.UUID assigns the id during a real persist(); the mock repository
		// skips that, so assign it here the way Hibernate would.
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(UUID.randomUUID());
			return user;
		});
		when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

		AuthResponse response = authService.register(new RegisterRequest("new@example.com", "password123"));

		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.userId()).isNotBlank();
	}

	@Test
	void loginRejectsUnknownEmail() {
		when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "password123")))
			.isInstanceOf(ApiException.class)
			.hasMessage("Invalid email or password");
	}

	@Test
	void loginRejectsWrongPassword() {
		User user = newUser("user@example.com", "hashed");
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong")))
			.isInstanceOf(ApiException.class)
			.hasMessage("Invalid email or password");
	}

	@Test
	void loginReturnsTokenOnSuccess() {
		User user = newUser("user@example.com", "hashed");
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		AuthResponse response = authService.login(new LoginRequest("user@example.com", "correct"));

		assertThat(response.token()).isEqualTo("jwt-token");
	}

	private User newUser(String email, String passwordHash) {
		User user = new User(email, passwordHash);
		user.setId(UUID.randomUUID());
		return user;
	}

}
