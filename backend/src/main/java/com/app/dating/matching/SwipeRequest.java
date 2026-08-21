package com.app.dating.matching;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SwipeRequest(
	@NotNull UUID toUserId,
	@NotNull SwipeDirection direction
) {
}
