package com.app.dating.matching;

import com.app.dating.auth.User;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePhoto;
import com.app.dating.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

	@Mock
	private MatchRepository matchRepository;

	@Mock
	private ProfileRepository profileRepository;

	private MatchService matchService;
	private UUID me;
	private UUID them;

	@BeforeEach
	void setUp() {
		matchService = new MatchService(matchRepository, profileRepository);
		me = UUID.randomUUID();
		them = UUID.randomUUID();
	}

	@Test
	void mapsOtherUserFromViewerPerspective() {
		UUID userAId = me.compareTo(them) < 0 ? me : them;
		UUID userBId = me.compareTo(them) < 0 ? them : me;
		Match match = new Match(userAId, userBId, Instant.now().plus(48, ChronoUnit.HOURS));
		match.setId(UUID.randomUUID());
		when(matchRepository.findAllForUser(me)).thenReturn(List.of(match));

		User themUser = new User("them@example.com", "hashed");
		themUser.setId(them);
		Profile themProfile = new Profile(themUser, "Them", LocalDate.of(1996, 3, 4));
		themProfile.getPhotos().add(new ProfilePhoto(themProfile, "/uploads/them.jpg", 0));
		when(profileRepository.findAllById(List.of(them))).thenReturn(List.of(themProfile));

		MatchesResponse response = matchService.listMyMatches(me);

		assertThat(response.matches()).hasSize(1);
		MatchSummaryDto summary = response.matches().get(0);
		assertThat(summary.otherUser().userId()).isEqualTo(them.toString());
		assertThat(summary.otherUser().name()).isEqualTo("Them");
		assertThat(summary.otherUser().photos()).containsExactly("/uploads/them.jpg");
		assertThat(summary.openingMoveDone()).isFalse();
	}

	@Test
	void fallsBackToPlaceholderWhenOtherProfileMissing() {
		UUID userAId = me.compareTo(them) < 0 ? me : them;
		UUID userBId = me.compareTo(them) < 0 ? them : me;
		Match match = new Match(userAId, userBId, Instant.now().plus(48, ChronoUnit.HOURS));
		match.setId(UUID.randomUUID());
		when(matchRepository.findAllForUser(me)).thenReturn(List.of(match));
		when(profileRepository.findAllById(List.of(them))).thenReturn(List.of());

		MatchesResponse response = matchService.listMyMatches(me);

		assertThat(response.matches().get(0).otherUser().name()).isEqualTo("Unknown");
	}

}
