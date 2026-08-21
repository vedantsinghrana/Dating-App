package com.app.dating.matching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchExpiryJobTest {

	@Mock
	private MatchRepository matchRepository;

	@Test
	void delegatesToRepositoryDeleteWithCurrentTime() {
		when(matchRepository.deleteExpiredWithoutOpeningMove(any(Instant.class))).thenReturn(3);

		new MatchExpiryJob(matchRepository).expireStaleMatches();

		verify(matchRepository).deleteExpiredWithoutOpeningMove(any(Instant.class));
	}

}
