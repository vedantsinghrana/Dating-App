package com.app.dating.discovery;

import com.app.dating.auth.User;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopPickServiceTest {

	@Mock
	private DailyTopPickRepository dailyTopPickRepository;

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private DiscoveryRepository discoveryRepository;

	private TopPickService topPickService;
	private UUID me;

	@BeforeEach
	void setUp() {
		topPickService = new TopPickService(dailyTopPickRepository, profileRepository, discoveryRepository);
		me = UUID.randomUUID();
	}

	@Test
	void returnsEmptyWhenNoPickForToday() {
		when(dailyTopPickRepository.findByUserIdAndPickDate(me, LocalDate.now())).thenReturn(Optional.empty());

		assertThat(topPickService.getTodaysTopPick(me)).isEmpty();
	}

	@Test
	void returnsDiscoveryShapedResultWhenPickExists() {
		UUID pickedId = UUID.randomUUID();
		DailyTopPick pick = new DailyTopPick(me, pickedId, LocalDate.now(), 0.85);
		when(dailyTopPickRepository.findByUserIdAndPickDate(me, LocalDate.now())).thenReturn(Optional.of(pick));

		User pickedUser = new User("picked@example.com", "hashed");
		pickedUser.setId(pickedId);
		Profile pickedProfile = new Profile(pickedUser, "Picked", LocalDate.of(1997, 1, 1));
		when(profileRepository.findById(pickedId)).thenReturn(Optional.of(pickedProfile));
		when(discoveryRepository.distanceMetersBetween(me, pickedId)).thenReturn(Optional.of(4200.0));

		Optional<DiscoveryResultDto> result = topPickService.getTodaysTopPick(me);

		assertThat(result).isPresent();
		assertThat(result.get().userId()).isEqualTo(pickedId.toString());
		assertThat(result.get().name()).isEqualTo("Picked");
		assertThat(result.get().distanceKm()).isEqualTo(4.2);
	}

	@Test
	void returnsEmptyWhenPickedProfileNoLongerExists() {
		UUID pickedId = UUID.randomUUID();
		DailyTopPick pick = new DailyTopPick(me, pickedId, LocalDate.now(), 0.85);
		when(dailyTopPickRepository.findByUserIdAndPickDate(me, LocalDate.now())).thenReturn(Optional.of(pick));
		when(profileRepository.findById(pickedId)).thenReturn(Optional.empty());

		assertThat(topPickService.getTodaysTopPick(me)).isEmpty();
	}

}
