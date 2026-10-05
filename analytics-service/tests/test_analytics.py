from datetime import datetime, timedelta, timezone

from fastapi.testclient import TestClient

from app.analytics import analyze_performance
from app.main import app
from app.models import AnalyticsInput

client = TestClient(app)
TEST_TOKEN = "local-test-token"
TEST_USER_ID = "0f56bd8d-fad4-4f47-a55a-d0406cd9b2ea"


def sample_data() -> AnalyticsInput:
    now = datetime.now(timezone.utc)
    rows = []
    for index in range(14):
        rows.append({
            "topic": "Arrays" if index < 7 else "Graphs",
            "difficulty": "BEGINNER" if index < 7 else "ADVANCED",
            "status": "ATTEMPTED" if index < 7 else "SOLVED",
            "attempts": 2 if index < 7 else 1,
            "timeTakenSeconds": 300 + index * 10,
            "occurredAt": (now - timedelta(days=13 - index)).isoformat(),
        })
    return AnalyticsInput.model_validate({"attempts": rows})


def test_performance_uses_explainable_topic_difficulty_and_time_statistics() -> None:
    report = analyze_performance(sample_data())

    assert report.total_attempts == 14
    assert report.solved_attempts == 7
    assert report.overall_accuracy == 50.0
    assert report.average_solving_time_seconds == 400.0
    assert [item.topic for item in report.topic_accuracy] == ["Arrays", "Graphs"]
    assert report.topic_accuracy[0].accuracy == 0.0
    assert report.topic_accuracy[1].accuracy == 100.0
    assert [item.difficulty for item in report.difficulty_performance] == ["BEGINNER", "ADVANCED"]
    assert report.weak_topics[0].topic == "Arrays"
    assert report.improvement_trend.direction == "IMPROVING"
    assert len(report.weekly_trend) == 8


def test_empty_history_returns_safe_zero_metrics() -> None:
    report = analyze_performance(AnalyticsInput(attempts=[]))

    assert report.overall_accuracy == 0.0
    assert report.average_solving_time_seconds is None
    assert report.consistency_score == 0.0
    assert report.weak_topics == []
    assert report.improvement_trend.direction == "INSUFFICIENT_DATA"


def test_internal_endpoints_require_service_token(monkeypatch) -> None:
    monkeypatch.setenv("ANALYTICS_SERVICE_TOKEN", TEST_TOKEN)
    response = client.post(
        f"/analytics/user/{TEST_USER_ID}/performance",
        json=sample_data().model_dump(mode="json", by_alias=True),
    )

    assert response.status_code == 401


def test_authenticated_internal_endpoint_returns_performance(monkeypatch) -> None:
    monkeypatch.setenv("ANALYTICS_SERVICE_TOKEN", TEST_TOKEN)
    response = client.post(
        f"/analytics/user/{TEST_USER_ID}/performance",
        headers={"X-Internal-Token": TEST_TOKEN},
        json=sample_data().model_dump(mode="json", by_alias=True),
    )

    assert response.status_code == 200
    assert response.json()["overallAccuracy"] == 50.0
    assert response.json()["topicAccuracy"][0]["topic"] == "Arrays"