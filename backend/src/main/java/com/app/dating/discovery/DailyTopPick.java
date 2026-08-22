package com.app.dating.discovery;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_top_picks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyTopPick {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "picked_user_id", nullable = false)
	private UUID pickedUserId;

	@Column(name = "pick_date", nullable = false)
	private LocalDate pickDate;

	@Column(nullable = false)
	private double score;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public DailyTopPick(UUID userId, UUID pickedUserId, LocalDate pickDate, double score) {
		this.userId = userId;
		this.pickedUserId = pickedUserId;
		this.pickDate = pickDate;
		this.score = score;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

}
