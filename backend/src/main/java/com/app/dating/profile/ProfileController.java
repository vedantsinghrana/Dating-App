package com.app.dating.profile;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping("/me")
	public ProfileResponse getMyProfile(Authentication authentication) {
		return profileService.getMyProfile(currentUserId(authentication));
	}

	@PutMapping("/me")
	public ProfileResponse updateMyProfile(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
		return profileService.upsertMyProfile(currentUserId(authentication), request);
	}

	@PostMapping(value = "/me/photos", consumes = "multipart/form-data")
	public PhotoUploadResponse uploadPhoto(Authentication authentication, @RequestParam("file") MultipartFile file) {
		return profileService.addPhoto(currentUserId(authentication), file);
	}

	@PutMapping("/me/location")
	public ResponseEntity<Void> updateLocation(Authentication authentication, @Valid @RequestBody LocationUpdateRequest request) {
		profileService.updateLocation(currentUserId(authentication), request);
		return ResponseEntity.noContent().build();
	}

	private UUID currentUserId(Authentication authentication) {
		return (UUID) authentication.getPrincipal();
	}

}
