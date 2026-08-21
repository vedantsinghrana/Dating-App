package com.app.dating.profile;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * id is null/omitted when the client is adding a new prompt via PUT /profiles/me;
 * when it matches an existing prompt on this profile, that prompt is updated in place.
 */
public record PromptDto(
	UUID id,
	@NotBlank String question,
	@NotBlank String answer
) {
}
