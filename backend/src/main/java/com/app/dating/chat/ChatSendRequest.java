package com.app.dating.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatSendRequest(@NotNull UUID matchId, @NotBlank String content) {
}
