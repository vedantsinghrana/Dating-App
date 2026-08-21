package com.app.dating.discovery;

import com.app.dating.auth.User;
import com.app.dating.common.ApiException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	@Mock
	private DiscoveryRepository discoveryRepository;

	@Mock
	private ProfileRepository profileRepository;

	private DiscoveryService discoveryService;
	private UUID me;
	private Profile myProfile;

	@BeforeEach
	void setUp() {
		discoveryService = new DiscoveryService(discoveryRepository, profileRepository, 2);
		me = UUID.randomUUID();
		User user = new User("me@example.com", "hashed");
		user.setId(me);
		myProfile = new Profile(user, "Me", LocalDate.of(1995, 1, 1));
		myProfile.setSearchRadiusKm(25);
	}

	@Test
	void throwsWhenNoProfile() {
		when(profileRepository.findById(me)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> discoveryService.discover(me, null, 0))
			.isInstanceOf(ApiException.class)
			.hasMessage("Create your profile before using discovery");
	}

	@Test
	void throwsWhenNoLocation() {
		when(profileRepository.findById(me)).thenReturn(Optional.of(myProfile));

		assertThatThrownBy(() -> discoveryService.discover(me, null, 0))
			.isInstanceOf(ApiException.class)
			.hasMessage("Set your location before using discovery");
	}

	@Test
	void usesProfileRadiusWhenNotSpecifiedAndTrimsExtraRow() {
		myProfile.setLocation(point(12.97, 77.59));
		when(profileRepository.findById(me)).thenReturn(Optional.of(myProfile));

		UUID a = UUID.randomUUID();
		UUID b = UUID.randomUUID();
		UUID c = UUID.randomUUID();
		when(discoveryRepository.findNearby(eq(me), anyDouble(), anyDouble(), eq(25000.0), eq(3), eq(0)))
			.thenReturn(List.of(row(a, 1000.0), row(b, 2000.0), row(c, 3000.0)));

		Profile pa = otherProfile(a, "A");
		Profile pb = otherProfile(b, "B");
		when(profileRepository.findAllById(List.of(a, b))).thenReturn(List.of(pa, pb));

		DiscoveryResponse response = discoveryService.discover(me, null, 0);

		assertThat(response.hasMore()).isTrue();
		assertThat(response.results()).hasSize(2);
		assertThat(response.results().get(0).userId()).isEqualTo(a.toString());
		assertThat(response.results().get(0).distanceKm()).isEqualTo(1.0);
		assertThat(response.results().get(1).userId()).isEqualTo(b.toString());
	}

	@Test
	void usesExplicitRadiusOverProfileDefault() {
		myProfile.setLocation(point(12.97, 77.59));
		when(profileRepository.findById(me)).thenReturn(Optional.of(myProfile));
		when(discoveryRepository.findNearby(any(), anyDouble(), anyDouble(), eq(10000.0), anyInt(), anyInt()))
			.thenReturn(List.of());

		discoveryService.discover(me, 10, 0);
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

	private Profile otherProfile(UUID id, String name) {
		User user = new User(name.toLowerCase() + "@example.com", "hashed");
		user.setId(id);
		return new Profile(user, name, LocalDate.of(1997, 6, 1));
	}

	private Point point(double lat, double lng) {
		Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
		point.setSRID(4326);
		return point;
	}

}
