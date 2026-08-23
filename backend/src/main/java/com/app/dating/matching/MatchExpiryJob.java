package com.app.dating.matching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Deletes matches that hit their 48h expiry without a first message ever being sent
 * (opening_move_done stays false until the messages feature sets it).
 *
 * fixedRate scheduling runs its first execution almost immediately on startup (no
 * initialDelay), which is fine in production but races against test-managed data in a
 * real Spring context — see app.match.expiry-job.enabled, disabled in integration tests.
 */
@Component
@ConditionalOnProperty(name = "app.match.expiry-job.enabled", havingValue = "true", matchIfMissing = true)
public class MatchExpiryJob {

	private static final Logger log = LoggerFactory.getLogger(MatchExpiryJob.class);

	private final MatchRepository matchRepository;

	public MatchExpiryJob(MatchRepository matchRepository) {
		this.matchRepository = matchRepository;
	}

	@Scheduled(fixedRateString = "${app.match.expiry-check-interval-ms:900000}")
	@Transactional
	public void expireStaleMatches() {
		int deleted = matchRepository.deleteExpiredWithoutOpeningMove(Instant.now());
		if (deleted > 0) {
			log.info("Expired {} match(es) with no opening move sent", deleted);
		}
	}

}
