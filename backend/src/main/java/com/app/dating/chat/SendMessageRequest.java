package com.app.dating.chat;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SendMessageRequest(@NotBlank String content, UUID promptId) {
}
