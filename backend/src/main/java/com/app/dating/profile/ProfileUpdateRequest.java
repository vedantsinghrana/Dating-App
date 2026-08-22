package com.app.dating.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

public record ProfileUpdateRequest(
	@NotBlank String name,
	@NotNull @Past LocalDate birthdate,
	String bio,
	List<@NotBlank String> photos,
	@Valid List<PromptDto> prompts,
	LocationDto location,
	Integer searchRadiusKm
) {
}
