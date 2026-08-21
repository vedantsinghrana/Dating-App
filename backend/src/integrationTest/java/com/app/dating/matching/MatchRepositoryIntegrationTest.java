package com.app.dating.matching;

import com.app.dating.AbstractIntegrationTest;
import com.app.dating.auth.User;
import com.app.dating.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class MatchRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MatchRepository matchRepository;

	@Test
	void deletesOnlyExpiredMatchesWithoutAnOpeningMove() {
		Match expiredNoOpeningMove = save(match(Instant.now().minus(1, ChronoUnit.HOURS), false));
		Match expiredWithOpeningMove = save(match(Instant.now().minus(1, ChronoUnit.HOURS), true));
		Match notYetExpired = save(match(Instant.now().plus(1, ChronoUnit.HOURS), false));

		int deleted = matchRepository.deleteExpiredWithoutOpeningMove(Instant.now());

		assertThat(deleted).isEqualTo(1);
		assertThat(matchRepository.findById(expiredNoOpeningMove.getId())).isEmpty();
		assertThat(matchRepository.findById(expiredWithOpeningMove.getId())).isPresent();
		assertThat(matchRepository.findById(notYetExpired.getId())).isPresent();
	}

	private Match match(Instant expiresAt, boolean openingMoveDone) {
		User userA = userRepository.save(new User(UUID.randomUUID() + "@example.com", "hashed"));
		User userB = userRepository.save(new User(UUID.randomUUID() + "@example.com", "hashed"));
		UUID a = userA.getId().compareTo(userB.getId()) < 0 ? userA.getId() : userB.getId();
		UUID b = userA.getId().compareTo(userB.getId()) < 0 ? userB.getId() : userA.getId();
		Match match = new Match(a, b, expiresAt);
		match.setOpeningMoveDone(openingMoveDone);
		return match;
	}

	private Match save(Match match) {
		return matchRepository.save(match);
	}

}
