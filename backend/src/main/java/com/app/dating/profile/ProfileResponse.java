package com.app.dating.profile;

import java.time.LocalDate;
import java.util.List;

public record ProfileResponse(
	String userId,
	String name,
	LocalDate birthdate,
	String bio,
	List<String> photos,
	List<PromptDto> prompts,
	LocationDto location,
	Integer searchRadiusKm
) {
}
