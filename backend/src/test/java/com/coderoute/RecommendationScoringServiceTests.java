package com.coderoute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.coderoute.entity.enums.Difficulty;
import com.coderoute.recommendation.RecommendationScoringService;
import com.coderoute.recommendation.RecommendationScoringService.CandidateSignals;

class RecommendationScoringServiceTests {
	private final RecommendationScoringService scorer = new RecommendationScoringService();
	private final Instant now = Instant.parse("2026-10-01T12:00:00Z");

	@Test
	void weakerTopicAccuracyProducesHigherRecommendationScore() {
		CandidateSignals weak = candidate(42.0, Difficulty.BEGINNER);
		CandidateSignals strong = candidate(88.0, Difficulty.ADVANCED);

		assertTrue(scorer.score(weak, now) > scorer.score(strong, now));
	}

	@Test
	void difficultyFitFavorsBeginnerWorkForLowAccuracy() {
		CandidateSignals beginner = candidate(35.0, Difficulty.BEGINNER);
		CandidateSignals advanced = candidate(35.0, Difficulty.ADVANCED);

		assertTrue(scorer.score(beginner, now) > scorer.score(advanced, now));
	}

	@Test
	void recentFailuresAndRepeatedAttemptsIncreaseTopicPriority() {
		CandidateSignals noFailure = signals(55.0, 1, 1, null, 0, 1, false, 0.0, null);
		CandidateSignals struggled = signals(55.0, 1, 4, Difficulty.INTERMEDIATE, 0, 1, false, 0.0, null);

		assertTrue(scorer.score(struggled, now) > scorer.score(noFailure, now));
		assertTrue(scorer.reason(struggled).contains("recently struggled with Medium problems"));
	}

	@Test
	void masteredPrerequisitesAndUrgentGoalIncreaseScore() {
		CandidateSignals unsupported = signals(72.0, 0, 0, null, 0, 2, false, 0.0, null);
		CandidateSignals readyAndUrgent = signals(72.0, 0, 0, null, 2, 2, true, 0.9, null);

		assertTrue(scorer.score(readyAndUrgent, now) > scorer.score(unsupported, now));
		assertTrue(scorer.reason(readyAndUrgent).contains("active learning goal"));
	}

	@Test
	void recentRepeatIsPenalizedAndSolvedProblemGetsLargePenalty() {
		CandidateSignals stale = signals(70.0, 2, 0, null, 0, 0, false, 0.0, now.minusSeconds(30L * 86400));
		CandidateSignals recent = signals(70.0, 2, 0, null, 0, 0, false, 0.0, now.minusSeconds(3600));
		CandidateSignals solved = new CandidateSignals("Binary Search", Difficulty.INTERMEDIATE, 25, true,
				70.0, Difficulty.INTERMEDIATE, 2, 0, 0, null, null, 1200.0, 0, 0, false, 0.0);

		assertTrue(scorer.score(stale, now) > scorer.score(recent, now));
		assertEquals(0.0, scorer.score(solved, now));
	}

	@Test
	void excessiveTimeTakenLowersTheTimeFitComponent() {
		CandidateSignals onPace = signals(70.0, 2, 0, null, 0, 0, false, 0.0, null);
		CandidateSignals overTime = new CandidateSignals("Binary Search", Difficulty.INTERMEDIATE, 25, false,
				70.0, Difficulty.INTERMEDIATE, 2, 0, 0, null, null, 3600.0, 0, 0, false, 0.0);

		assertTrue(scorer.score(onPace, now) > scorer.score(overTime, now));
	}

	@Test
	void lowAccuracyReasonIncludesMeasuredPerformance() {
		CandidateSignals signals = signals(48.0, 3, 2, Difficulty.INTERMEDIATE, 1, 1, false, 0.0, now);

		assertEquals("Recommended because your accuracy in Binary Search is 48% and you recently struggled with Medium problems.",
				scorer.reason(signals));
	}

	private CandidateSignals candidate(Double accuracy, Difficulty difficulty) {
		return signals(accuracy, 3, 0, null, 0, 0, false, 0.0, null, difficulty);
	}

	private CandidateSignals signals(Double accuracy, int failures, int failureAttempts,
			Difficulty failureDifficulty, int masteredPrerequisites, int prerequisites,
			boolean hasGoal, double goalUrgency, Instant lastAttemptAt) {
		return signals(accuracy, failures, failureAttempts, failureDifficulty, masteredPrerequisites,
				prerequisites, hasGoal, goalUrgency, lastAttemptAt, Difficulty.INTERMEDIATE);
	}

	private CandidateSignals signals(Double accuracy, int failures, int failureAttempts,
			Difficulty failureDifficulty, int masteredPrerequisites, int prerequisites,
			boolean hasGoal, double goalUrgency, Instant lastAttemptAt, Difficulty difficulty) {
		return new CandidateSignals("Binary Search", difficulty, 25, false, accuracy,
				Difficulty.INTERMEDIATE, 3, failures, failureAttempts, failureDifficulty,
				lastAttemptAt, 1500.0, masteredPrerequisites, prerequisites, hasGoal, goalUrgency);
	}
}