package com.app.dating.discovery;

import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePhoto;
import com.app.dating.profile.ProfileRepository;
import com.app.dating.profile.PromptDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TopPickService {

	private final DailyTopPickRepository dailyTopPickRepository;
	private final ProfileRepository profileRepository;
	private final DiscoveryRepository discoveryRepository;

	public TopPickService(
		DailyTopPickRepository dailyTopPickRepository,
		ProfileRepository profileRepository,
		DiscoveryRepository discoveryRepository
	) {
		this.dailyTopPickRepository = dailyTopPickRepository;
		this.profileRepository = profileRepository;
		this.discoveryRepository = discoveryRepository;
	}

	@Transactional(readOnly = true)
	public Optional<DiscoveryResultDto> getTodaysTopPick(UUID me) {
		Optional<DailyTopPick> pick = dailyTopPickRepository.findByUserIdAndPickDate(me, LocalDate.now());
		if (pick.isEmpty()) {
			return Optional.empty();
		}
		UUID pickedUserId = pick.get().getPickedUserId();
		Optional<Profile> pickedProfile = profileRepository.findById(pickedUserId);
		if (pickedProfile.isEmpty()) {
			return Optional.empty();
		}
		double distanceKm = discoveryRepository.distanceMetersBetween(me, pickedUserId).orElse(0.0) / 1000.0;
		return Optional.of(toResult(pickedProfile.get(), distanceKm));
	}

	private DiscoveryResultDto toResult(Profile profile, double distanceKm) {
		List<String> photos = profile.getPhotos().stream().map(ProfilePhoto::getUrl).toList();
		List<PromptDto> prompts = profile.getPrompts().stream()
			.map(p -> new PromptDto(p.getId(), p.getQuestion(), p.getAnswer()))
			.toList();
		int age = Period.between(profile.getBirthdate(), LocalDate.now()).getYears();
		return new DiscoveryResultDto(profile.getId().toString(), profile.getName(), age, photos, prompts, distanceKm);
	}

}
