package com.app.dating.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Setter
	private UUID id;

	@Column(name = "match_id", nullable = false)
	private UUID matchId;

	@Column(name = "sender_id", nullable = false)
	private UUID senderId;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "prompt_id")
	private UUID promptId;

	@Column(name = "sent_at", nullable = false, updatable = false)
	private Instant sentAt;

	public Message(UUID matchId, UUID senderId, String content, UUID promptId) {
		this.matchId = matchId;
		this.senderId = senderId;
		this.content = content;
		this.promptId = promptId;
	}

	@PrePersist
	void onCreate() {
		this.sentAt = Instant.now();
	}

}
