package com.app.dating.discovery;

import com.app.dating.AbstractIntegrationTest;
import com.app.dating.auth.User;
import com.app.dating.auth.UserRepository;
import com.app.dating.matching.Match;
import com.app.dating.matching.MatchRepository;
import com.app.dating.matching.Swipe;
import com.app.dating.matching.SwipeDirection;
import com.app.dating.matching.SwipeRepository;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bangalore-area coordinates: MG Road as "me", a point ~2km away ("near", inside a
 * 5km radius), and a point far outside Bangalore entirely ("far").
 */
@Transactional
class DiscoveryRepositoryIntegrationTest extends AbstractIntegrationTest {

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
	private static final double ME_LAT = 12.9716;
	private static final double ME_LNG = 77.5946;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private DiscoveryRepository discoveryRepository;

	@Autowired
	private SwipeRepository swipeRepository;

	@Autowired
	private MatchRepository matchRepository;

	@Test
	void findsOnlyNearbyUnswipedUnmatchedProfilesOrderedByDistance() {
		User me = createUser("me@example.com");
		createProfile(me, "Me", ME_LAT, ME_LNG);

		User near = createUser("near@example.com");
		createProfile(near, "Near", 12.9800, 77.6000); // ~1.3km away

		User far = createUser("far@example.com");
		createProfile(far, "Far", 19.0760, 72.8777); // Mumbai, hundreds of km away

		User alreadySwiped = createUser("swiped@example.com");
		createProfile(alreadySwiped, "Swiped", 12.9750, 77.5980);
		swipeRepository.save(new Swipe(me.getId(), alreadySwiped.getId(), SwipeDirection.PASS));

		User alreadyMatched = createUser("matched@example.com");
		createProfile(alreadyMatched, "Matched", 12.9750, 77.5980);
		UUID a = me.getId().compareTo(alreadyMatched.getId()) < 0 ? me.getId() : alreadyMatched.getId();
		UUID b = me.getId().compareTo(alreadyMatched.getId()) < 0 ? alreadyMatched.getId() : me.getId();
		matchRepository.save(new Match(a, b, Instant.now().plus(48, ChronoUnit.HOURS)));

		List<DiscoveryRow> results = discoveryRepository.findNearby(me.getId(), ME_LAT, ME_LNG, 5_000, 10, 0);

		assertThat(results).extracting(DiscoveryRow::getId).containsExactly(near.getId());
	}

	@Test
	void ordersByDistanceAscending() {
		User me = createUser("me2@example.com");
		createProfile(me, "Me", ME_LAT, ME_LNG);

		User closer = createUser("closer@example.com");
		createProfile(closer, "Closer", 12.9720, 77.5950);

		User fartherButInRadius = createUser("farther@example.com");
		createProfile(fartherButInRadius, "Farther", 12.9900, 77.6100);

		List<DiscoveryRow> results = discoveryRepository.findNearby(me.getId(), ME_LAT, ME_LNG, 10_000, 10, 0);

		assertThat(results).extracting(DiscoveryRow::getId)
			.containsExactly(closer.getId(), fartherButInRadius.getId());
	}

	private User createUser(String email) {
		return userRepository.save(new User(email, "hashed"));
	}

	private void createProfile(User user, String name, double lat, double lng) {
		Profile profile = new Profile(user, name, LocalDate.of(1997, 1, 1));
		profile.setLocation(point(lat, lng));
		profileRepository.save(profile);
	}

	private Point point(double lat, double lng) {
		Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
		point.setSRID(4326);
		return point;
	}

}
