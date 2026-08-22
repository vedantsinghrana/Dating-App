package com.app.dating.discovery;

import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePrompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Weighted-scoring "Daily Top Pick": no ML, just an honest, explainable heuristic
 * (see PROJECT_PLAN.md) blending distance, shared-prompt overlap (our closest proxy for
 * shared interests, since profiles don't have a separate interest-tag model), and how
 * recently the candidate was last active.
 */
@Service
public class TopPickScoringService {

	private static final double PROMPT_OVERLAP_CAP = 3.0;
	private static final double ACTIVITY_DECAY_HOURS = 24.0 * 7;

	private final double weightDistance;
	private final double weightPromptOverlap;
	private final double weightRecentActivity;

	public TopPickScoringService(
		@Value("${app.top-pick.weight-distance:0.4}") double weightDistance,
		@Value("${app.top-pick.weight-prompt-overlap:0.3}") double weightPromptOverlap,
		@Value("${app.top-pick.weight-recent-activity:0.3}") double weightRecentActivity
	) {
		this.weightDistance = weightDistance;
		this.weightPromptOverlap = weightPromptOverlap;
		this.weightRecentActivity = weightRecentActivity;
	}

	public double score(Profile viewer, Profile candidate, double distanceKm, double radiusKm, Instant candidateLastActiveAt, Instant now) {
		double distanceScore = distanceScore(distanceKm, radiusKm);
		double promptScore = promptOverlapScore(viewer, candidate);
		double activityScore = recentActivityScore(candidateLastActiveAt, now);
		return weightDistance * distanceScore + weightPromptOverlap * promptScore + weightRecentActivity * activityScore;
	}

	private double distanceScore(double distanceKm, double radiusKm) {
		if (radiusKm <= 0) {
			return 0;
		}
		return clamp(1 - (distanceKm / radiusKm));
	}

	private double promptOverlapScore(Profile viewer, Profile candidate) {
		Set<String> viewerQuestions = normalizedQuestions(viewer);
		if (viewerQuestions.isEmpty()) {
			return 0;
		}
		long overlap = candidate.getPrompts().stream()
			.map(ProfilePrompt::getQuestion)
			.map(this::normalize)
			.filter(viewerQuestions::contains)
			.count();
		return clamp(overlap / PROMPT_OVERLAP_CAP);
	}

	private double recentActivityScore(Instant lastActiveAt, Instant now) {
		if (lastActiveAt == null) {
			return 0;
		}
		double hoursSinceActive = Duration.between(lastActiveAt, now).toMinutes() / 60.0;
		return clamp(1 - (hoursSinceActive / ACTIVITY_DECAY_HOURS));
	}

	private Set<String> normalizedQuestions(Profile profile) {
		return profile.getPrompts().stream().map(ProfilePrompt::getQuestion).map(this::normalize).collect(Collectors.toSet());
	}

	private String normalize(String question) {
		return question == null ? "" : question.trim().toLowerCase();
	}

	private double clamp(double value) {
		return Math.max(0, Math.min(1, value));
	}

}
