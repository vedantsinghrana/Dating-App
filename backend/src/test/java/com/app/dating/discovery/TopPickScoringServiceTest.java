package com.app.dating.discovery;

import com.app.dating.auth.User;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePrompt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TopPickScoringServiceTest {

	private final TopPickScoringService scoringService = new TopPickScoringService(0.4, 0.3, 0.3);

	@Test
	void closerCandidateScoresHigherThanFartherOneAllElseEqual() {
		Profile viewer = profile("Viewer");
		Profile close = profile("Close");
		Profile far = profile("Far");
		Instant now = Instant.now();

		double closeScore = scoringService.score(viewer, close, 1.0, 25.0, now, now);
		double farScore = scoringService.score(viewer, far, 20.0, 25.0, now, now);

		assertThat(closeScore).isGreaterThan(farScore);
	}

	@Test
	void sharedPromptQuestionsIncreaseScore() {
		Profile viewer = profile("Viewer");
		viewer.getPrompts().add(new ProfilePrompt(viewer, "Two truths and a lie", "A", 0));

		Profile matchingCandidate = profile("Matching");
		matchingCandidate.getPrompts().add(new ProfilePrompt(matchingCandidate, "Two Truths And A Lie", "B", 0));

		Profile nonMatchingCandidate = profile("NonMatching");
		nonMatchingCandidate.getPrompts().add(new ProfilePrompt(nonMatchingCandidate, "My simple pleasures", "C", 0));

		Instant now = Instant.now();
		double matchingScore = scoringService.score(viewer, matchingCandidate, 5.0, 25.0, now, now);
		double nonMatchingScore = scoringService.score(viewer, nonMatchingCandidate, 5.0, 25.0, now, now);

		assertThat(matchingScore).isGreaterThan(nonMatchingScore);
	}

	@Test
	void recentlyActiveCandidateScoresHigherThanStaleOne() {
		Profile viewer = profile("Viewer");
		Profile recentlyActive = profile("Recent");
		Profile staleActive = profile("Stale");
		Instant now = Instant.now();

		double recentScore = scoringService.score(viewer, recentlyActive, 5.0, 25.0, now.minus(1, ChronoUnit.HOURS), now);
		double staleScore = scoringService.score(viewer, staleActive, 5.0, 25.0, now.minus(30, ChronoUnit.DAYS), now);

		assertThat(recentScore).isGreaterThan(staleScore);
	}

	@Test
	void neverActiveCandidateGetsZeroActivityContributionNotNegative() {
		Profile viewer = profile("Viewer");
		Profile neverActive = profile("NeverActive");
		Instant now = Instant.now();

		double score = scoringService.score(viewer, neverActive, 5.0, 25.0, null, now);

		assertThat(score).isGreaterThanOrEqualTo(0);
	}

	private Profile profile(String name) {
		User user = new User(name.toLowerCase() + "@example.com", "hashed");
		user.setId(UUID.randomUUID());
		return new Profile(user, name, LocalDate.of(1997, 1, 1));
	}

}
