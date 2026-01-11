from django.urls import path
from .views import (
    # 러닝 관련
    RunningSessionListCreateView,
    RunningSessionDetailView,
    RunningSummaryTodayView,
    RunningSummaryWeekView,
    RunningSummary7DaysView,
    RunningStatsView,
    
    # 퀘스트 관련
    AvailableQuestListAPIView,
    UserQuestProgressListAPIView,
    ClaimQuestRewardAPIView,
)

urlpatterns = [
    # 1. 러닝 (Running)
    path("", RunningSessionListCreateView.as_view(), name="running-list"),
    path("<int:pk>/", RunningSessionDetailView.as_view(), name="running-detail"),
    path("today/", RunningSummaryTodayView.as_view(), name="running-today"),
    path("week/", RunningSummaryWeekView.as_view(), name="running-week"),
    path("7days/", RunningSummary7DaysView.as_view(), name="running-7days"),
    path("stats/", RunningStatsView.as_view(), name="running-stats"),

    # 2. 퀘스트 (Quest)
    path("quests/", AvailableQuestListAPIView.as_view(), name="quest-list"),
    path("my-quests/", UserQuestProgressListAPIView.as_view(), name="my-quests"),
    path("quests/<int:pk>/claim/", ClaimQuestRewardAPIView.as_view(), name="quest-claim"),
]