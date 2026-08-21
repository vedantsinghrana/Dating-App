package com.app.dating.discovery;

import com.app.dating.profile.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DiscoveryRepository extends Repository<Profile, UUID> {

	/**
	 * Nearby profiles within radiusMeters of (lat, lng), excluding the viewer, anyone the
	 * viewer has already swiped on, and anyone already matched with the viewer. Ordered by
	 * distance ascending; fetches one extra row so the caller can compute hasMore cheaply.
	 */
	@Query(value = """
		SELECT p.id AS id, ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) AS distanceM
		FROM profiles p
		WHERE p.id <> :me
		  AND p.location IS NOT NULL
		  AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
		  AND NOT EXISTS (SELECT 1 FROM swipes s WHERE s.swiper_id = :me AND s.swipee_id = p.id)
		  AND NOT EXISTS (
		      SELECT 1 FROM matches m
		      WHERE (m.user_a_id = :me AND m.user_b_id = p.id)
		         OR (m.user_a_id = p.id AND m.user_b_id = :me)
		  )
		ORDER BY distanceM ASC
		LIMIT :limit OFFSET :offset
		""", nativeQuery = true)
	List<DiscoveryRow> findNearby(
		@Param("me") UUID me,
		@Param("lat") double lat,
		@Param("lng") double lng,
		@Param("radiusMeters") double radiusMeters,
		@Param("limit") int limit,
		@Param("offset") int offset
	);

}
