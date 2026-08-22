package com.app.dating.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

	boolean existsByUserAIdAndUserBId(UUID userAId, UUID userBId);

	@Query("SELECT m FROM Match m WHERE m.userAId = :userId OR m.userBId = :userId ORDER BY m.createdAt DESC")
	List<Match> findAllForUser(@Param("userId") UUID userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Match m WHERE m.openingMoveDone = false AND m.expiresAt <= :now")
	int deleteExpiredWithoutOpeningMove(@Param("now") Instant now);

}
