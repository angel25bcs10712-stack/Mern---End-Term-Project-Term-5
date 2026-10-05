from collections import defaultdict
from datetime import datetime, time, timedelta, timezone

from app.models import (
    AnalyticsInput,
    Attempt,
    AttemptStatus,
    Difficulty,
    DifficultyPerformance,
    PerformanceReport,
    TopicAccuracy,
    TrendComparison,
    TrendReport,
    WeakTopic,
    WeeklyTrend,
)

CONSISTENCY_TARGET_DAYS_PER_WEEK = 4
CONSISTENCY_WINDOW_DAYS = 28
MIN_WEAK_TOPIC_ATTEMPTS = 3
WEAK_TOPIC_ACCURACY_THRESHOLD = 60.0
TREND_WINDOW_SIZE = 7
WEEK_WINDOW_COUNT = 8


def _is_solved(attempt: Attempt) -> bool:
    return attempt.status == AttemptStatus.SOLVED


def _accuracy(attempts: list[Attempt]) -> float:
    if not attempts:
        return 0.0
    return round(sum(_is_solved(attempt) for attempt in attempts) * 100.0 / len(attempts), 2)


def _average_solve_time(attempts: list[Attempt]) -> float | None:
    durations = [attempt.time_taken_seconds for attempt in attempts if _is_solved(attempt)]
    if not durations:
        return None
    return round(sum(durations) / len(durations), 2)


def _comparison(attempts: list[Attempt]) -> TrendComparison:
    ordered = sorted(attempts, key=lambda attempt: attempt.occurred_at)
    window = min(TREND_WINDOW_SIZE, len(ordered) // 2)
    if window < 3:
        return TrendComparison(
            previous_accuracy=None,
            recent_accuracy=None,
            change_percentage_points=None,
            direction="INSUFFICIENT_DATA",
            previous_attempts=0,
            recent_attempts=0,
        )

    previous = ordered[-(2 * window) : -window]
    recent = ordered[-window:]
    previous_accuracy = _accuracy(previous)
    recent_accuracy = _accuracy(recent)
    change = round(recent_accuracy - previous_accuracy, 2)
    direction = "IMPROVING" if change >= 5 else "DECLINING" if change <= -5 else "STABLE"
    return TrendComparison(
        previous_accuracy=previous_accuracy,
        recent_accuracy=recent_accuracy,
        change_percentage_points=change,
        direction=direction,
        previous_attempts=len(previous),
        recent_attempts=len(recent),
    )


def _weekly_trend(attempts: list[Attempt], as_of: datetime) -> list[WeeklyTrend]:
    current_monday = (as_of - timedelta(days=as_of.weekday())).date()
    first_monday = current_monday - timedelta(weeks=WEEK_WINDOW_COUNT - 1)
    grouped: dict[str, list[Attempt]] = defaultdict(list)

    for attempt in attempts:
        week_start = (attempt.occurred_at.date() - timedelta(days=attempt.occurred_at.weekday())).isoformat()
        if first_monday.isoformat() <= week_start <= current_monday.isoformat():
            grouped[week_start].append(attempt)

    results = []
    for week_offset in range(WEEK_WINDOW_COUNT):
        week_start = (first_monday + timedelta(weeks=week_offset)).isoformat()
        week_attempts = grouped.get(week_start, [])
        solved = sum(_is_solved(attempt) for attempt in week_attempts)
        results.append(WeeklyTrend(
            week_start=week_start,
            attempt_count=len(week_attempts),
            solved_count=solved,
            accuracy=_accuracy(week_attempts),
        ))
    return results


def analyze_performance(data: AnalyticsInput, as_of: datetime | None = None) -> PerformanceReport:
    attempts = sorted(data.attempts, key=lambda attempt: attempt.occurred_at)
    current_time = as_of or datetime.now(timezone.utc)
    if current_time.tzinfo is None:
        current_time = current_time.replace(tzinfo=timezone.utc)
    else:
        current_time = current_time.astimezone(timezone.utc)

    by_topic: dict[str, list[Attempt]] = defaultdict(list)
    by_difficulty: dict[Difficulty, list[Attempt]] = defaultdict(list)
    for attempt in attempts:
        by_topic[attempt.topic].append(attempt)
        by_difficulty[attempt.difficulty].append(attempt)

    topic_metrics = [
        TopicAccuracy(
            topic=topic,
            attempt_count=len(topic_attempts),
            solved_count=sum(_is_solved(attempt) for attempt in topic_attempts),
            accuracy=_accuracy(topic_attempts),
            average_solving_time_seconds=_average_solve_time(topic_attempts),
        )
        for topic, topic_attempts in by_topic.items()
    ]
    topic_metrics.sort(key=lambda item: (item.accuracy, item.topic.casefold()))

    difficulty_metrics = []
    for difficulty in Difficulty:
        difficulty_attempts = by_difficulty.get(difficulty, [])
        if not difficulty_attempts:
            continue
        difficulty_metrics.append(DifficultyPerformance(
            difficulty=difficulty,
            attempt_count=len(difficulty_attempts),
            solved_count=sum(_is_solved(attempt) for attempt in difficulty_attempts),
            accuracy=_accuracy(difficulty_attempts),
            average_solving_time_seconds=_average_solve_time(difficulty_attempts),
        ))

    weak_topics = [
        WeakTopic(
            topic=metric.topic,
            attempt_count=metric.attempt_count,
            solved_count=metric.solved_count,
            accuracy=metric.accuracy,
            reason=(
                f"{metric.accuracy:.1f}% accuracy across {metric.attempt_count} attempts; "
                f"review this topic before increasing difficulty."
            ),
        )
        for metric in topic_metrics
        if metric.attempt_count >= MIN_WEAK_TOPIC_ATTEMPTS
        and metric.accuracy < WEAK_TOPIC_ACCURACY_THRESHOLD
    ]

    consistency_cutoff = current_time - timedelta(days=CONSISTENCY_WINDOW_DAYS)
    active_days = {
        attempt.occurred_at.astimezone(timezone.utc).date()
        for attempt in attempts
        if consistency_cutoff <= attempt.occurred_at.astimezone(timezone.utc) <= current_time
    }
    consistency_score = round(min(
        100.0,
        len(active_days) * 100.0 / (CONSISTENCY_TARGET_DAYS_PER_WEEK * 4),
    ), 2)
    solved_count = sum(_is_solved(attempt) for attempt in attempts)
    return PerformanceReport(
        total_attempts=len(attempts),
        solved_attempts=solved_count,
        overall_accuracy=_accuracy(attempts),
        average_solving_time_seconds=_average_solve_time(attempts),
        consistency_score=consistency_score,
        topic_accuracy=topic_metrics,
        difficulty_performance=difficulty_metrics,
        weak_topics=weak_topics,
        improvement_trend=_comparison(attempts),
        weekly_trend=_weekly_trend(attempts, current_time),
    )


def trend_report(data: AnalyticsInput, as_of: datetime | None = None) -> TrendReport:
    report = analyze_performance(data, as_of)
    return TrendReport(
        improvement_trend=report.improvement_trend,
        weekly_trend=report.weekly_trend,
    )