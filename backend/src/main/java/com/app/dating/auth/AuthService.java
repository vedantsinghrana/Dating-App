package com.app.dating.auth;

import com.app.dating.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Email already in use");
		}
		User user = new User(request.email(), passwordEncoder.encode(request.password()));
		userRepository.save(user);
		return new AuthResponse(jwtService.generateToken(user), user.getId().toString());
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmailIgnoreCase(request.email())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}
		user.setLastActiveAt(Instant.now());
		return new AuthResponse(jwtService.generateToken(user), user.getId().toString());
	}

}
