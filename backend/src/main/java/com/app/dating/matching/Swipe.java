package com.app.dating.matching;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "swipes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Swipe {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "swiper_id", nullable = false)
	private UUID swiperId;

	@Column(name = "swipee_id", nullable = false)
	private UUID swipeeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 4)
	private SwipeDirection direction;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public Swipe(UUID swiperId, UUID swipeeId, SwipeDirection direction) {
		this.swiperId = swiperId;
		this.swipeeId = swipeeId;
		this.direction = direction;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

}
