package com.app.dating.chat;

import java.time.Instant;

public record MessageResponse(String id, String senderId, String content, Instant sentAt) {

	static MessageResponse from(Message message) {
		return new MessageResponse(
			message.getId().toString(),
			message.getSenderId().toString(),
			message.getContent(),
			message.getSentAt()
		);
	}

}
