from django.urls import path
from .views import (
    RunningSessionListCreateView,
    AvailableQuestListAPIView,
    UserQuestProgressListAPIView,
    ClaimQuestRewardAPIView,
    QuestProgressView,
)

urlpatterns = [
    path("", RunningSessionListCreateView.as_view(), name="running-session-list-create"),

    # Quest
    path("quests/", AvailableQuestListAPIView.as_view(), name="quest-list"),
    path("my-quests/", UserQuestProgressListAPIView.as_view(), name="user-quests"),
    path("claim/<int:pk>/", ClaimQuestRewardAPIView.as_view(), name="claim-quest"),
    path("quests/progress/", QuestProgressView.as_view(), name="quest-progress"),
]
