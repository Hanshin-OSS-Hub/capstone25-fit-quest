from django.urls import path
from .views import (
    RunningSessionListCreateView,
    RunningSessionDetailView,
    RunningSummaryTodayView,
    RunningSummaryWeekView,
    RunningSummary7DaysView,
    RunningStatsView,
)

urlpatterns = [
    path("running/", RunningSessionListCreateView.as_view(), name="running-list-create"),
    path("running/<int:pk>/", RunningSessionDetailView.as_view(), name="running-detail"),

    # 요약 API
    path("running/summary/today/", RunningSummaryTodayView.as_view(), name="running-summary-today"),
    path("running/summary/week/", RunningSummaryWeekView.as_view(), name="running-summary-week"),
    path("running/summary/7days/", RunningSummary7DaysView.as_view(), name="running-summary-7days"),

    # 전체 통계
    path("running/stats/", RunningStatsView.as_view(), name="running-stats"),
]
