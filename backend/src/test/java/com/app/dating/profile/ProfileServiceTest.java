package com.app.dating.profile;

import com.app.dating.auth.User;
import com.app.dating.auth.UserRepository;
import com.app.dating.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PhotoStorageService photoStorageService;

	private ProfileService profileService;
	private UUID userId;
	private User user;

	@BeforeEach
	void setUp() {
		profileService = new ProfileService(profileRepository, userRepository, photoStorageService);
		userId = UUID.randomUUID();
		user = new User("person@example.com", "hashed");
		user.setId(userId);
	}

	@Test
	void getMyProfileThrowsWhenMissing() {
		when(profileRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> profileService.getMyProfile(userId))
			.isInstanceOf(ApiException.class)
			.hasMessage("Profile not found");
	}

	@Test
	void upsertCreatesProfileWhenNoneExists() {
		when(profileRepository.findById(userId)).thenReturn(Optional.empty());
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

		ProfileUpdateRequest request = new ProfileUpdateRequest(
			"Alex", LocalDate.of(1998, 4, 12), "Hi there",
			List.of("url1", "url2"),
			List.of(new PromptDto(null, "Two truths and a lie", "I code, I hike, I lie")),
			new LocationDto(12.97, 77.59),
			15
		);

		ProfileResponse response = profileService.upsertMyProfile(userId, request);

		assertThat(response.userId()).isEqualTo(userId.toString());
		assertThat(response.name()).isEqualTo("Alex");
		assertThat(response.photos()).containsExactly("url1", "url2");
		assertThat(response.prompts()).hasSize(1);
		assertThat(response.location()).isEqualTo(new LocationDto(12.97, 77.59));
		assertThat(response.searchRadiusKm()).isEqualTo(15);
	}

	@Test
	void upsertUpdatesExistingPromptInPlaceAndDropsRemoved() {
		Profile profile = new Profile(user, "Alex", LocalDate.of(1998, 4, 12));
		ProfilePrompt keep = new ProfilePrompt(profile, "Old question", "Old answer", 0);
		ProfilePrompt drop = new ProfilePrompt(profile, "Going away", "Bye", 1);
		profile.getPrompts().add(keep);
		profile.getPrompts().add(drop);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
		when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

		ProfileUpdateRequest request = new ProfileUpdateRequest(
			"Alex", LocalDate.of(1998, 4, 12), "Hi there",
			List.of(),
			List.of(
				new PromptDto(keep.getId(), "Updated question", "Updated answer"),
				new PromptDto(null, "Brand new prompt", "Brand new answer")
			),
			null,
			null
		);

		ProfileResponse response = profileService.upsertMyProfile(userId, request);

		assertThat(response.prompts()).hasSize(2);
		assertThat(response.prompts().get(0).id()).isEqualTo(keep.getId());
		assertThat(response.prompts().get(0).question()).isEqualTo("Updated question");
		assertThat(response.prompts()).extracting(PromptDto::question).doesNotContain("Going away");
	}

	@Test
	void upsertWithoutLocationLeavesExistingLocationUnchanged() {
		Profile profile = new Profile(user, "Alex", LocalDate.of(1998, 4, 12));
		when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
		when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

		profileService.updateLocation(userId, new LocationUpdateRequest(12.97, 77.59));

		ProfileUpdateRequest request = new ProfileUpdateRequest(
			"Alex", LocalDate.of(1998, 4, 12), "Updated bio", List.of(), List.of(), null, null
		);
		ProfileResponse response = profileService.upsertMyProfile(userId, request);

		assertThat(response.location()).isEqualTo(new LocationDto(12.97, 77.59));
	}

	@Test
	void updateLocationThrowsWhenProfileMissing() {
		when(profileRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> profileService.updateLocation(userId, new LocationUpdateRequest(1.0, 2.0)))
			.isInstanceOf(ApiException.class)
			.hasMessage("Profile not found");
	}

	@Test
	void addPhotoAppendsToExistingPhotos() {
		Profile profile = new Profile(user, "Alex", LocalDate.of(1998, 4, 12));
		profile.getPhotos().add(new ProfilePhoto(profile, "/uploads/existing.jpg", 0));
		when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
		MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
		when(photoStorageService.store(file)).thenReturn("/uploads/new.jpg");

		PhotoUploadResponse response = profileService.addPhoto(userId, file);

		assertThat(response.url()).isEqualTo("/uploads/new.jpg");
		assertThat(profile.getPhotos()).extracting(ProfilePhoto::getUrl)
			.containsExactly("/uploads/existing.jpg", "/uploads/new.jpg");
	}

	@Test
	void addPhotoThrowsWhenProfileMissing() {
		when(profileRepository.findById(userId)).thenReturn(Optional.empty());
		MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});

		assertThatThrownBy(() -> profileService.addPhoto(userId, file))
			.isInstanceOf(ApiException.class)
			.hasMessage("Profile not found");
	}

}
