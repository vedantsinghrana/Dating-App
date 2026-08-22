package com.app.dating.discovery;

import com.app.dating.auth.User;
import com.app.dating.auth.UserRepository;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Runs once daily (default 03:00 server time) and picks, for every user with a located
 * profile, the single highest-scoring nearby candidate — see TopPickScoringService for
 * the weighting. A per-user failure is logged and skipped rather than aborting the batch.
 */
@Component
public class DailyTopPickJob {

	private static final Logger log = LoggerFactory.getLogger(DailyTopPickJob.class);
	private static final int CANDIDATE_POOL_SIZE = 100;

	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;
	private final DiscoveryRepository discoveryRepository;
	private final DailyTopPickRepository dailyTopPickRepository;
	private final TopPickScoringService scoringService;

	public DailyTopPickJob(
		ProfileRepository profileRepository,
		UserRepository userRepository,
		DiscoveryRepository discoveryRepository,
		DailyTopPickRepository dailyTopPickRepository,
		TopPickScoringService scoringService
	) {
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
		this.discoveryRepository = discoveryRepository;
		this.dailyTopPickRepository = dailyTopPickRepository;
		this.scoringService = scoringService;
	}

	@Scheduled(cron = "${app.top-pick.cron:0 0 3 * * *}")
	public void computeDailyTopPicks() {
		LocalDate today = LocalDate.now();
		List<Profile> viewers = profileRepository.findByLocationIsNotNull();
		int computed = 0;
		for (Profile viewer : viewers) {
			try {
				if (computeForViewer(viewer, today)) {
					computed++;
				}
			} catch (Exception ex) {
				log.warn("Failed to compute daily top pick for user {}", viewer.getId(), ex);
			}
		}
		log.info("Computed {} daily top pick(s) for {}", computed, today);
	}

	@Transactional
	protected boolean computeForViewer(Profile viewer, LocalDate today) {
		double radiusKm = viewer.getSearchRadiusKm();
		List<DiscoveryRow> candidates = discoveryRepository.findNearby(
			viewer.getId(), viewer.getLocation().getY(), viewer.getLocation().getX(), radiusKm * 1000, CANDIDATE_POOL_SIZE, 0
		);
		if (candidates.isEmpty()) {
			return false;
		}

		List<UUID> candidateIds = candidates.stream().map(DiscoveryRow::getId).toList();
		Map<UUID, Profile> profilesById = profileRepository.findAllById(candidateIds).stream()
			.collect(java.util.stream.Collectors.toMap(Profile::getId, Function.identity()));
		Map<UUID, User> usersById = userRepository.findAllById(candidateIds).stream()
			.collect(java.util.stream.Collectors.toMap(User::getId, Function.identity()));

		Instant now = Instant.now();
		UUID bestCandidateId = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (DiscoveryRow row : candidates) {
			Profile candidateProfile = profilesById.get(row.getId());
			if (candidateProfile == null) {
				continue;
			}
			User candidateUser = usersById.get(row.getId());
			Instant lastActiveAt = candidateUser == null ? null : candidateUser.getLastActiveAt();
			double distanceKm = row.getDistanceM() / 1000.0;
			double score = scoringService.score(viewer, candidateProfile, distanceKm, radiusKm, lastActiveAt, now);
			if (score > bestScore) {
				bestScore = score;
				bestCandidateId = row.getId();
			}
		}

		if (bestCandidateId == null) {
			return false;
		}
		dailyTopPickRepository.deleteByUserIdAndPickDate(viewer.getId(), today);
		dailyTopPickRepository.save(new DailyTopPick(viewer.getId(), bestCandidateId, today, bestScore));
		return true;
	}

}
