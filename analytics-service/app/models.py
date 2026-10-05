from datetime import datetime
from enum import StrEnum

from pydantic import BaseModel, ConfigDict, Field


def to_camel(value: str) -> str:
    first, *rest = value.split("_")
    return first + "".join(part.capitalize() for part in rest)


class AnalyticsModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class Difficulty(StrEnum):
    BEGINNER = "BEGINNER"
    INTERMEDIATE = "INTERMEDIATE"
    ADVANCED = "ADVANCED"


class AttemptStatus(StrEnum):
    ATTEMPTED = "ATTEMPTED"
    SOLVED = "SOLVED"
    ABANDONED = "ABANDONED"


class Attempt(AnalyticsModel):
    topic: str = Field(min_length=1, max_length=120)
    difficulty: Difficulty
    status: AttemptStatus
    attempts: int = Field(ge=1, le=100)
    time_taken_seconds: int = Field(ge=0, le=86400)
    occurred_at: datetime


class AnalyticsInput(AnalyticsModel):
    attempts: list[Attempt] = Field(max_length=2000)


class TopicAccuracy(AnalyticsModel):
    topic: str
    attempt_count: int
    solved_count: int
    accuracy: float
    average_solving_time_seconds: float | None


class DifficultyPerformance(AnalyticsModel):
    difficulty: Difficulty
    attempt_count: int
    solved_count: int
    accuracy: float
    average_solving_time_seconds: float | None


class WeakTopic(AnalyticsModel):
    topic: str
    attempt_count: int
    solved_count: int
    accuracy: float
    reason: str


class TrendComparison(AnalyticsModel):
    previous_accuracy: float | None
    recent_accuracy: float | None
    change_percentage_points: float | None
    direction: str
    previous_attempts: int
    recent_attempts: int


class WeeklyTrend(AnalyticsModel):
    week_start: str
    attempt_count: int
    solved_count: int
    accuracy: float


class PerformanceReport(AnalyticsModel):
    total_attempts: int
    solved_attempts: int
    overall_accuracy: float
    average_solving_time_seconds: float | None
    consistency_score: float
    topic_accuracy: list[TopicAccuracy]
    difficulty_performance: list[DifficultyPerformance]
    weak_topics: list[WeakTopic]
    improvement_trend: TrendComparison
    weekly_trend: list[WeeklyTrend]


class TrendReport(AnalyticsModel):
    improvement_trend: TrendComparison
    weekly_trend: list[WeeklyTrend]