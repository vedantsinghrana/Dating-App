package com.app.dating.chat;

import com.app.dating.auth.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The SockJS/WebSocket HTTP handshake itself is left open in SecurityConfig (it can't
 * carry a normal Authorization header from a browser); auth instead happens on the STOMP
 * CONNECT frame, which the client sends with an "Authorization: Bearer <jwt>" native header.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private final JwtService jwtService;

	public StompAuthChannelInterceptor(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			String authHeader = accessor.getFirstNativeHeader("Authorization");
			UUID userId = extractUserId(authHeader)
				.orElseThrow(() -> new org.springframework.messaging.MessagingException("Missing or invalid Authorization header"));
			accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
		}
		return message;
	}

	private Optional<UUID> extractUserId(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return Optional.empty();
		}
		return jwtService.extractUserId(authHeader.substring("Bearer ".length()));
	}

}
