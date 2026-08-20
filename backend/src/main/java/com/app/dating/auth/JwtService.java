package com.app.dating.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long expirationMs;

	public JwtService(
		@Value("${app.jwt.secret}") String secret,
		@Value("${app.jwt.expiration-ms}") long expirationMs
	) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
			.subject(user.getId().toString())
			.claim("email", user.getEmail())
			.issuedAt(now)
			.expiration(expiry)
			.signWith(signingKey)
			.compact();
	}

	public Optional<UUID> extractUserId(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
			return Optional.of(UUID.fromString(claims.getSubject()));
		} catch (JwtException | IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

}
