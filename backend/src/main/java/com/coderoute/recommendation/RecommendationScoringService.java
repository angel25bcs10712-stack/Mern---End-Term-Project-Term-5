package com.coderoute.recommendation;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.coderoute.entity.enums.Difficulty;

@Service
public class RecommendationScoringService {
	private static final double TOPIC_WEAKNESS_WEIGHT = 24.0;
	private static final double SUCCESS_RATE_WEIGHT = 18.0;
	private static final double RECENT_FAILURE_WEIGHT = 15.0;
	private static final double DIFFICULTY_FIT_WEIGHT = 18.0;
	private static final double PREREQUISITE_WEIGHT = 12.0;
	private static final double LEARNING_GOAL_WEIGHT = 10.0;
	private static final double TIME_FIT_WEIGHT = 8.0;
	private static final double ATTEMPT_LOAD_WEIGHT = 6.0;
	private static final double RECENCY_PENALTY = 12.0;
	private static final double SOLVED_PENALTY = 100.0;

	public double score(CandidateSignals candidate, Instant now) {
		// Scores are intentionally additive and bounded so each factor can be tuned independently.
		double accuracy = candidate.topicAccuracyPercent() == null
				? 50.0
				: clamp(candidate.topicAccuracyPercent(), 0.0, 100.0);
		double topicWeakness = candidate.topicAccuracyPercent() == null
				? 10.0
				: TOPIC_WEAKNESS_WEIGHT * (1.0 - accuracy / 100.0);
		double successRate = candidate.topicAccuracyPercent() == null
				? SUCCESS_RATE_WEIGHT * 0.4
				: SUCCESS_RATE_WEIGHT * (1.0 - accuracy / 100.0);
		double recentFailures = Math.min(RECENT_FAILURE_WEIGHT,
				candidate.recentFailureCount() * 3.0 + Math.max(0, candidate.recentFailureAttempts() - 1) * 1.5);
		double difficultyFit = difficultyFit(candidate, accuracy);
		double prerequisiteFit = prerequisiteFit(candidate);
		double learningGoalFit = candidate.hasActiveGoal()
				? LEARNING_GOAL_WEIGHT * clamp(candidate.goalUrgency(), 0.0, 1.0)
						* (candidate.estimatedTimeMinutes() <= 45 ? 1.0 : 0.6)
				: 0.0;
		double timeFit = timeFit(candidate);
		double attemptLoad = Math.min(ATTEMPT_LOAD_WEIGHT,
				Math.max(0, candidate.recentFailureAttempts() - 1) * 2.0);
		double recency = recencyAdjustment(candidate.lastAttemptAt(), now);
		double solvedPenalty = candidate.solved() ? SOLVED_PENALTY : 0.0;

		return Math.max(0.0, topicWeakness + successRate + recentFailures + difficultyFit
				+ prerequisiteFit + learningGoalFit + timeFit + attemptLoad + recency - solvedPenalty);
	}

	public String reason(CandidateSignals candidate) {
		String difficulty = displayDifficulty(candidate.difficulty());
		Double accuracy = candidate.topicAccuracyPercent();
		if (accuracy != null && accuracy < 60.0 && candidate.recentFailureCount() > 0) {
			return "Recommended because your accuracy in " + candidate.topicName() + " is "
					+ Math.round(accuracy) + "% and you recently struggled with "
					+ displayDifficulty(candidate.recentFailureDifficulty()) + " problems.";
		}
		if (accuracy != null && accuracy < 70.0) {
			return "Recommended to strengthen your " + candidate.topicName() + " results; your current accuracy is "
					+ Math.round(accuracy) + "%. This " + difficulty.toLowerCase() + " problem is a fitting next step.";
		}
		if (candidate.hasActiveGoal() && candidate.goalUrgency() >= 0.5) {
			return "Recommended to support your active learning goal with a " + candidate.estimatedTimeMinutes()
					+ " minute " + difficulty.toLowerCase() + " practice session.";
		}
		if (candidate.prerequisiteCount() > 0 && candidate.masteredPrerequisiteCount() == candidate.prerequisiteCount()) {
			return "Recommended because your progress in the prerequisite topics prepares you for "
					+ candidate.topicName() + ".";
		}
		if (candidate.attemptedProblems() == 0) {
			return "Recommended as a " + difficulty.toLowerCase() + " introduction to "
					+ candidate.topicName() + " and a useful starting point for your progress history.";
		}
		return "Recommended as a " + difficulty.toLowerCase() + " next step in " + candidate.topicName()
				+ " based on your recent practice and time taken.";
	}

	private double difficultyFit(CandidateSignals candidate, double accuracy) {
		Difficulty target;
		if (accuracy < 50.0) target = Difficulty.BEGINNER;
		else if (accuracy < 78.0) target = Difficulty.INTERMEDIATE;
		else target = Difficulty.ADVANCED;
		if (candidate.currentDifficulty() != null && accuracy >= 50.0 && accuracy < 78.0) {
			target = candidate.currentDifficulty();
		}
		int distance = Math.abs(target.ordinal() - candidate.difficulty().ordinal());
		return distance == 0 ? DIFFICULTY_FIT_WEIGHT : distance == 1 ? DIFFICULTY_FIT_WEIGHT * 0.55 : 1.0;
	}

	private double prerequisiteFit(CandidateSignals candidate) {
		if (candidate.prerequisiteCount() == 0) return PREREQUISITE_WEIGHT * 0.5;
		return PREREQUISITE_WEIGHT * candidate.masteredPrerequisiteCount() / candidate.prerequisiteCount();
	}

	private double timeFit(CandidateSignals candidate) {
		if (candidate.averageTimeTakenSeconds() == null || candidate.averageTimeTakenSeconds() <= 0) {
			return TIME_FIT_WEIGHT * 0.6;
		}
		double estimatedSeconds = candidate.estimatedTimeMinutes() * 60.0;
		double ratio = candidate.averageTimeTakenSeconds() / estimatedSeconds;
		if (ratio > 1.75) return TIME_FIT_WEIGHT * 0.2;
		if (ratio > 1.25) return TIME_FIT_WEIGHT * 0.55;
		if (ratio >= 0.6) return TIME_FIT_WEIGHT;
		return TIME_FIT_WEIGHT * 0.7;
	}

	private double recencyAdjustment(Instant lastAttemptAt, Instant now) {
		if (lastAttemptAt == null) return 3.0;
		long days = Math.max(0, Duration.between(lastAttemptAt, now).toDays());
		if (days < 1) return -RECENCY_PENALTY;
		if (days < 7) return -RECENCY_PENALTY * 0.6;
		if (days < 21) return -RECENCY_PENALTY * 0.25;
		return 1.0;
	}

	private String displayDifficulty(Difficulty difficulty) {
		if (difficulty == null) return "recent";
		return switch (difficulty) {
			case BEGINNER -> "Easy";
			case INTERMEDIATE -> "Medium";
			case ADVANCED -> "Hard";
		};
	}

	private double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	public record CandidateSignals(
			String topicName,
			Difficulty difficulty,
			int estimatedTimeMinutes,
			boolean solved,
			Double topicAccuracyPercent,
			Difficulty currentDifficulty,
			int attemptedProblems,
			int recentFailureCount,
			int recentFailureAttempts,
			Difficulty recentFailureDifficulty,
			Instant lastAttemptAt,
			Double averageTimeTakenSeconds,
			int masteredPrerequisiteCount,
			int prerequisiteCount,
			boolean hasActiveGoal,
			double goalUrgency) {
	}
}