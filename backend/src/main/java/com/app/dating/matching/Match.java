package com.app.dating.matching;

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

/**
 * userAId is always the lexicographically smaller of the two user ids (enforced by the
 * ck_matches_ordered_pair DB constraint), so a pair only ever produces one row.
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_a_id", nullable = false)
	private UUID userAId;

	@Column(name = "user_b_id", nullable = false)
	private UUID userBId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "opening_move_done", nullable = false)
	private boolean openingMoveDone = false;

	public Match(UUID userAId, UUID userBId, Instant expiresAt) {
		this.userAId = userAId;
		this.userBId = userBId;
		this.expiresAt = expiresAt;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

	public UUID otherUserId(UUID viewerId) {
		return userAId.equals(viewerId) ? userBId : userAId;
	}

}
