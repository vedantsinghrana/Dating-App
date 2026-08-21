package com.app.dating.matching;

import java.time.Instant;

public record MatchSummaryDto(
	String matchId,
	OtherUserDto otherUser,
	Instant createdAt,
	Instant expiresAt,
	boolean openingMoveDone
) {
}
