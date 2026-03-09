from django.urls import path
from .views import (
    # 러닝 기록 및 통계
    RunningSessionListCreateView,
    RunningSessionDetailView,
    RunningSummaryTodayView,
    RunningSummary7DaysView,
    RunningStatsView,
    
    # Health Connect (필요시 포함)
    # HealthConnectRunningUploadView,
    
    # 퀘스트 및 보상
    AvailableQuestListAPIView,
    UserQuestProgressListAPIView,
    ClaimQuestRewardAPIView,
)

urlpatterns = [
    # 1. 러닝 기록 (Running Sessions)
    path("", RunningSessionListCreateView.as_view(), name="running-list"),
    path("<int:pk>/", RunningSessionDetailView.as_view(), name="running-detail"),
    #path("hc/", HealthConnectRunningUploadView.as_view(), name="running-hc"), # 외부 데이터 연동

    # 2. 러닝 통계 요약 (Stats Summary)
    path("today/", RunningSummaryTodayView.as_view(), name="running-today"),
    path("7days/", RunningSummary7DaysView.as_view(), name="running-7days"),
    path("stats/", RunningStatsView.as_view(), name="running-stats"),

    # 3. 퀘스트 시스템 (Quest System)
    path("quests/", AvailableQuestListAPIView.as_view(), name="quest-list"),
    path("my-quests/", UserQuestProgressListAPIView.as_view(), name="my-quests"),
    path("quests/<int:pk>/claim/", ClaimQuestRewardAPIView.as_view(), name="quest-claim"),
]