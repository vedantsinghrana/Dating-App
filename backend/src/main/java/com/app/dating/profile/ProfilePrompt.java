package com.app.dating.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_prompts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfilePrompt {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_id", nullable = false)
	private Profile profile;

	@Column(nullable = false, columnDefinition = "text")
	private String question;

	@Column(nullable = false, columnDefinition = "text")
	private String answer;

	@Column(nullable = false)
	private Integer position;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public ProfilePrompt(Profile profile, String question, String answer, Integer position) {
		this.profile = profile;
		this.question = question;
		this.answer = answer;
		this.position = position;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

}
