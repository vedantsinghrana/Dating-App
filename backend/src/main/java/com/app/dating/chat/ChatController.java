package com.app.dating.chat;

import com.app.dating.common.ApiException;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class ChatController {

	private final MessageService messageService;

	public ChatController(MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * WS sends carry no promptId (see API_CONTRACT.md), so the opening-move rule can only
	 * be satisfied over the REST endpoint — MessageService rejects an opening move sent here.
	 */
	@MessageMapping("/chat.send")
	public void send(@Valid @Payload ChatSendRequest request, Principal principal) {
		UUID me = UUID.fromString(principal.getName());
		messageService.sendMessage(me, request.matchId(), new SendMessageRequest(request.content(), null));
	}

	@MessageExceptionHandler(ApiException.class)
	@SendToUser("/queue/errors")
	public Map<String, String> handleApiException(ApiException ex) {
		return Map.of("message", ex.getMessage());
	}

}
