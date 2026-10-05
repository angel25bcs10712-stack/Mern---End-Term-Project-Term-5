import os
import secrets
from typing import Annotated
from uuid import UUID

from fastapi import Depends, FastAPI, Header, HTTPException, status

from app.analytics import analyze_performance
from app.models import AnalyticsInput, PerformanceReport, TrendReport, WeakTopic

app = FastAPI(title="CodeRoute Analytics", version="1.0.0")


def require_internal_token(x_internal_token: Annotated[str | None, Header()] = None) -> None:
    expected = os.getenv("ANALYTICS_SERVICE_TOKEN", "")
    if not expected:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Analytics service is not configured.",
        )
    if not x_internal_token or not secrets.compare_digest(x_internal_token, expected):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized.")


InternalAccess = Annotated[None, Depends(require_internal_token)]


@app.post(
    "/analytics/user/{user_id}/performance",
    response_model=PerformanceReport,
)
def user_performance(user_id: UUID, data: AnalyticsInput, _: InternalAccess) -> PerformanceReport:
    del user_id
    return analyze_performance(data)


@app.post(
    "/analytics/user/{user_id}/weak-topics",
    response_model=list[WeakTopic],
)
def user_weak_topics(user_id: UUID, data: AnalyticsInput, _: InternalAccess) -> list[WeakTopic]:
    del user_id
    return analyze_performance(data).weak_topics


@app.post(
    "/analytics/user/{user_id}/trend",
    response_model=TrendReport,
)
def user_trend(user_id: UUID, data: AnalyticsInput, _: InternalAccess) -> TrendReport:
    del user_id
    report = analyze_performance(data)
    return TrendReport(improvement_trend=report.improvement_trend, weekly_trend=report.weekly_trend)