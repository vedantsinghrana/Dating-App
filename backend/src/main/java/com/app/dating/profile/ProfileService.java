package com.app.dating.profile;

import com.app.dating.auth.User;
import com.app.dating.auth.UserRepository;
import com.app.dating.common.ApiException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService {

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;
	private final PhotoStorageService photoStorageService;

	public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, PhotoStorageService photoStorageService) {
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
		this.photoStorageService = photoStorageService;
	}

	@Transactional(readOnly = true)
	public ProfileResponse getMyProfile(UUID userId) {
		return toResponse(findProfileOrThrow(userId));
	}

	@Transactional
	public ProfileResponse upsertMyProfile(UUID userId, ProfileUpdateRequest request) {
		Profile profile = profileRepository.findById(userId).orElseGet(() -> {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
			return new Profile(user, request.name(), request.birthdate());
		});

		profile.setName(request.name());
		profile.setBirthdate(request.birthdate());
		profile.setBio(request.bio());
		if (request.searchRadiusKm() != null) {
			profile.setSearchRadiusKm(request.searchRadiusKm());
		}
		if (request.location() != null) {
			profile.setLocation(toPoint(request.location()));
		}

		replacePhotos(profile, request.photos() == null ? List.of() : request.photos());
		replacePrompts(profile, request.prompts() == null ? List.of() : request.prompts());

		Profile saved = profileRepository.save(profile);
		return toResponse(saved);
	}

	@Transactional
	public void updateLocation(UUID userId, LocationUpdateRequest request) {
		Profile profile = findProfileOrThrow(userId);
		profile.setLocation(toPoint(new LocationDto(request.lat(), request.lng())));
	}

	@Transactional
	public PhotoUploadResponse addPhoto(UUID userId, MultipartFile file) {
		Profile profile = findProfileOrThrow(userId);
		String url = photoStorageService.store(file);
		int nextPosition = profile.getPhotos().size();
		profile.getPhotos().add(new ProfilePhoto(profile, url, nextPosition));
		return new PhotoUploadResponse(url);
	}

	private Profile findProfileOrThrow(UUID userId) {
		return profileRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
	}

	private void replacePhotos(Profile profile, List<String> urls) {
		profile.getPhotos().clear();
		for (int i = 0; i < urls.size(); i++) {
			profile.getPhotos().add(new ProfilePhoto(profile, urls.get(i), i));
		}
	}

	private void replacePrompts(Profile profile, List<PromptDto> prompts) {
		Map<UUID, ProfilePrompt> existingById = new HashMap<>();
		for (ProfilePrompt prompt : profile.getPrompts()) {
			existingById.put(prompt.getId(), prompt);
		}

		List<ProfilePrompt> updated = new ArrayList<>();
		for (int i = 0; i < prompts.size(); i++) {
			PromptDto dto = prompts.get(i);
			ProfilePrompt prompt = dto.id() != null ? existingById.get(dto.id()) : null;
			if (prompt != null) {
				prompt.setQuestion(dto.question());
				prompt.setAnswer(dto.answer());
				prompt.setPosition(i);
			} else {
				prompt = new ProfilePrompt(profile, dto.question(), dto.answer(), i);
			}
			updated.add(prompt);
		}

		profile.getPrompts().clear();
		profile.getPrompts().addAll(updated);
	}

	private Point toPoint(LocationDto location) {
		Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(location.lng(), location.lat()));
		point.setSRID(4326);
		return point;
	}

	private LocationDto toLocationDto(Point point) {
		return point == null ? null : new LocationDto(point.getY(), point.getX());
	}

	private ProfileResponse toResponse(Profile profile) {
		List<String> photoUrls = profile.getPhotos().stream().map(ProfilePhoto::getUrl).toList();
		List<PromptDto> prompts = profile.getPrompts().stream()
			.map(p -> new PromptDto(p.getId(), p.getQuestion(), p.getAnswer()))
			.toList();
		return new ProfileResponse(
			profile.getId().toString(),
			profile.getName(),
			profile.getBirthdate(),
			profile.getBio(),
			photoUrls,
			prompts,
			toLocationDto(profile.getLocation()),
			profile.getSearchRadiusKm()
		);
	}

}
