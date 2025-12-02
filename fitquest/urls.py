from django.urls import path, include
from fitquest.views import (
    SignupView,
    EmailTokenObtainPairView,
    MeView,
    KakaoLoginView,
)
from workout.views import (
    RunningSessionListCreateView,
    RunningSessionDetailView,
    RunningSummaryTodayView,
    RunningSummaryWeekView,
    RunningSummary7DaysView,
    RunningStatsView,
    AvailableQuestListAPIView,
    UserQuestProgressListAPIView,
    ClaimQuestRewardAPIView,
)

urlpatterns = [
    # Auth
    path("auth/signup/", SignupView.as_view()),
    path("auth/login/", EmailTokenObtainPairView.as_view()),
    path("auth/me/", MeView.as_view()),
    path("auth/kakao/", KakaoLoginView.as_view()),

    # Running
    path("running/", RunningSessionListCreateView.as_view()),
    path("running/<int:pk>/", RunningSessionDetailView.as_view()),
    path("running/today/", RunningSummaryTodayView.as_view()),
    path("running/week/", RunningSummaryWeekView.as_view()),
    path("running/7days/", RunningSummary7DaysView.as_view()),
    path("running/stats/", RunningStatsView.as_view()),

    # Quest System
    path("workout/quests/", AvailableQuestListAPIView.as_view(), name="quest-list"),
    path("workout/my-quests/", UserQuestProgressListAPIView.as_view(), name="my-quests"),
    path("workout/claim/<int:pk>/", ClaimQuestRewardAPIView.as_view(), name="claim-quest"),
]
