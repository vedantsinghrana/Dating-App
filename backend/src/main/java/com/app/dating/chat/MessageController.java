package com.app.dating.chat;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches/{matchId}/messages")
public class MessageController {

	private final MessageService messageService;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}

	@GetMapping
	public MessagesResponse listMessages(Authentication authentication, @PathVariable UUID matchId) {
		return messageService.listMessages(currentUserId(authentication), matchId);
	}

	@PostMapping
	public ResponseEntity<MessageResponse> sendMessage(
		Authentication authentication,
		@PathVariable UUID matchId,
		@Valid @RequestBody SendMessageRequest request
	) {
		MessageResponse response = messageService.sendMessage(currentUserId(authentication), matchId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	private UUID currentUserId(Authentication authentication) {
		return (UUID) authentication.getPrincipal();
	}

}
