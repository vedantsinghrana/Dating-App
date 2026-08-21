package com.app.dating.discovery;

import com.app.dating.profile.PromptDto;

import java.util.List;

public record DiscoveryResultDto(
	String userId,
	String name,
	int age,
	List<String> photos,
	List<PromptDto> prompts,
	double distanceKm
) {
}
