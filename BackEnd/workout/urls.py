from django.urls import path
from .views import (
    # 1. 일반 운동
    ExerciseListAPIView,
    ExerciseLogListCreateView,
    
    # 2. 러닝 및 통계
    RunningSessionListCreateView,
    RunningSessionDetailView,
    RunningSummaryTodayView,
    RunningSummary7DaysView,
    RunningStatsView,
    
    # 3. 헬스커넥트
    HealthConnectRunningUploadView,
    
    # 4. 퀘스트
    AvailableQuestListAPIView,
    UserQuestProgressListAPIView,
    ClaimQuestRewardAPIView,

    # 5. 홈 트레이닝 
    WorkoutListView,

    # 6. 업적 및 칭호 
    AchievementListAPIView,
)

urlpatterns = [
    # api/workout/ 접속 시 기본 페이지 
    path("", ExerciseListAPIView.as_view(), name="workout-index"), 
    
    # ==========================================
    # 1. 일반 운동 및 스트레칭
    # ==========================================
    path("exercises/", ExerciseListAPIView.as_view(), name="exercise-list"),
    path("exercises/logs/", ExerciseLogListCreateView.as_view(), name="exercise-log-list-create"),

    # ==========================================
    # 2. 러닝 기록 및 통계
    # ==========================================
    path("running/", RunningSessionListCreateView.as_view(), name="running-list"),
    path("running/<int:pk>/", RunningSessionDetailView.as_view(), name="running-detail"),
    path("running/today/", RunningSummaryTodayView.as_view(), name="running-today"),
    path("running/7days/", RunningSummary7DaysView.as_view(), name="running-7days"),
    path("running/stats/", RunningStatsView.as_view(), name="running-stats"),

    # ==========================================
    # 3. 헬스커넥트 연동
    # ==========================================
    path("running/hc/", HealthConnectRunningUploadView.as_view(), name="running-hc"),

    # ==========================================
    # 4. 퀘스트 시스템
    # ==========================================
    path("quests/", AvailableQuestListAPIView.as_view(), name="quest-list"),
    path("my-quests/", UserQuestProgressListAPIView.as_view(), name="my-quests"),
    path("quests/claim/<int:pk>/", ClaimQuestRewardAPIView.as_view(), name="quest-claim"),

    # ==========================================
    # 5. 홈 트레이닝 
    # ==========================================
    path("workouts/", WorkoutListView.as_view(), name="workout-list"),

    # ==========================================
    # 6. 업적 및 칭호 시스템 
    # ==========================================
    path("achievements/", AchievementListAPIView.as_view(), name="achievement-list"),
]