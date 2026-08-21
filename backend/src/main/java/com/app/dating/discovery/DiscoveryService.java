package com.app.dating.discovery;

import com.app.dating.common.ApiException;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePhoto;
import com.app.dating.profile.ProfileRepository;
import com.app.dating.profile.PromptDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DiscoveryService {

	private final DiscoveryRepository discoveryRepository;
	private final ProfileRepository profileRepository;
	private final int pageSize;

	public DiscoveryService(
		DiscoveryRepository discoveryRepository,
		ProfileRepository profileRepository,
		@Value("${app.discovery.page-size:20}") int pageSize
	) {
		this.discoveryRepository = discoveryRepository;
		this.profileRepository = profileRepository;
		this.pageSize = pageSize;
	}

	@Transactional(readOnly = true)
	public DiscoveryResponse discover(UUID me, Integer radiusKm, int page) {
		Profile myProfile = profileRepository.findById(me)
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Create your profile before using discovery"));
		if (myProfile.getLocation() == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Set your location before using discovery");
		}

		double effectiveRadiusKm = radiusKm != null ? radiusKm : myProfile.getSearchRadiusKm();
		double radiusMeters = effectiveRadiusKm * 1000;
		double lat = myProfile.getLocation().getY();
		double lng = myProfile.getLocation().getX();
		int offset = Math.max(page, 0) * pageSize;

		List<DiscoveryRow> rows = discoveryRepository.findNearby(me, lat, lng, radiusMeters, pageSize + 1, offset);
		boolean hasMore = rows.size() > pageSize;
		List<DiscoveryRow> pageRows = hasMore ? rows.subList(0, pageSize) : rows;

		List<UUID> ids = pageRows.stream().map(DiscoveryRow::getId).toList();
		Map<UUID, Profile> profilesById = new LinkedHashMap<>();
		for (Profile profile : profileRepository.findAllById(ids)) {
			profilesById.put(profile.getId(), profile);
		}

		List<DiscoveryResultDto> results = pageRows.stream()
			.map(row -> toResult(profilesById.get(row.getId()), row.getDistanceM()))
			.toList();

		return new DiscoveryResponse(results, hasMore);
	}

	private DiscoveryResultDto toResult(Profile profile, double distanceM) {
		List<String> photos = profile.getPhotos().stream().map(ProfilePhoto::getUrl).toList();
		List<PromptDto> prompts = profile.getPrompts().stream()
			.map(p -> new PromptDto(p.getId(), p.getQuestion(), p.getAnswer()))
			.toList();
		int age = Period.between(profile.getBirthdate(), LocalDate.now()).getYears();
		return new DiscoveryResultDto(
			profile.getId().toString(),
			profile.getName(),
			age,
			photos,
			prompts,
			distanceM / 1000.0
		);
	}

}
