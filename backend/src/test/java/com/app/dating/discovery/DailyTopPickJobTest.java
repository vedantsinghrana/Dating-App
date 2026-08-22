package com.app.dating.discovery;

import com.app.dating.auth.User;
import com.app.dating.auth.UserRepository;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyTopPickJobTest {

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DiscoveryRepository discoveryRepository;

	@Mock
	private DailyTopPickRepository dailyTopPickRepository;

	private DailyTopPickJob job;
	private TopPickScoringService scoringService;

	@BeforeEach
	void setUp() {
		scoringService = new TopPickScoringService(0.4, 0.3, 0.3);
		job = new DailyTopPickJob(profileRepository, userRepository, discoveryRepository, dailyTopPickRepository, scoringService);
	}

	@Test
	void picksHighestScoringCandidateAndPersists() {
		Profile viewer = profile("viewer@example.com", "Viewer", 12.97, 77.59);
		UUID closeId = UUID.randomUUID();
		UUID farId = UUID.randomUUID();
		when(profileRepository.findByLocationIsNotNull()).thenReturn(List.of(viewer));
		when(discoveryRepository.findNearby(eq(viewer.getId()), anyDouble(), anyDouble(), anyDouble(), any(Integer.class), eq(0)))
			.thenReturn(List.of(row(closeId, 500.0), row(farId, 20_000.0)));

		Profile closeProfile = profile("close@example.com", "Close", 12.98, 77.60);
		closeProfile.setId(closeId);
		Profile farProfile = profile("far@example.com", "Far", 13.10, 77.70);
		farProfile.setId(farId);
		when(profileRepository.findAllById(List.of(closeId, farId))).thenReturn(List.of(closeProfile, farProfile));
		when(userRepository.findAllById(List.of(closeId, farId))).thenReturn(List.of());

		job.computeDailyTopPicks();

		verify(dailyTopPickRepository).deleteByUserIdAndPickDate(eq(viewer.getId()), any());
		var captor = org.mockito.ArgumentCaptor.forClass(DailyTopPick.class);
		verify(dailyTopPickRepository).save(captor.capture());
		assertThat(captor.getValue().getPickedUserId()).isEqualTo(closeId);
	}

	@Test
	void skipsViewerWithNoCandidatesWithoutSavingAnything() {
		Profile viewer = profile("lonely@example.com", "Lonely", 12.97, 77.59);
		when(profileRepository.findByLocationIsNotNull()).thenReturn(List.of(viewer));
		when(discoveryRepository.findNearby(eq(viewer.getId()), anyDouble(), anyDouble(), anyDouble(), any(Integer.class), eq(0)))
			.thenReturn(List.of());

		job.computeDailyTopPicks();

		verify(dailyTopPickRepository, never()).save(any());
	}

	@Test
	void oneFailingViewerDoesNotStopOthersFromBeingProcessed() {
		Profile broken = profile("broken@example.com", "Broken", 12.97, 77.59);
		Profile healthy = profile("healthy@example.com", "Healthy", 12.97, 77.59);
		when(profileRepository.findByLocationIsNotNull()).thenReturn(List.of(broken, healthy));

		UUID candidateId = UUID.randomUUID();
		when(discoveryRepository.findNearby(eq(broken.getId()), anyDouble(), anyDouble(), anyDouble(), any(Integer.class), eq(0)))
			.thenThrow(new RuntimeException("boom"));
		when(discoveryRepository.findNearby(eq(healthy.getId()), anyDouble(), anyDouble(), anyDouble(), any(Integer.class), eq(0)))
			.thenReturn(List.of(row(candidateId, 1000.0)));
		Profile candidateProfile = profile("candidate@example.com", "Candidate", 12.98, 77.60);
		candidateProfile.setId(candidateId);
		when(profileRepository.findAllById(List.of(candidateId))).thenReturn(List.of(candidateProfile));
		when(userRepository.findAllById(List.of(candidateId))).thenReturn(List.of());

		job.computeDailyTopPicks();

		verify(dailyTopPickRepository, times(1)).save(any());
	}

	private DiscoveryRow row(UUID id, double distanceM) {
		return new DiscoveryRow() {
			public UUID getId() {
				return id;
			}

			public Double getDistanceM() {
				return distanceM;
			}
		};
	}

	private Profile profile(String email, String name, double lat, double lng) {
		User user = new User(email, "hashed");
		user.setId(UUID.randomUUID());
		Profile profile = new Profile(user, name, LocalDate.of(1997, 1, 1));
		profile.setSearchRadiusKm(25);
		profile.setLocation(point(lat, lng));
		return profile;
	}

	private Point point(double lat, double lng) {
		Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
		point.setSRID(4326);
		return point;
	}

}
