from django.urls import path
from .views import (
    SignupView,
    EmailTokenObtainPairView,
    MeView,
    KakaoLoginView,
    AchievementListView,
    MyTitleListView,
    RankingListView,
    
)

urlpatterns = [
    path("signup/", SignupView.as_view(), name="signup"),
    path("login/", EmailTokenObtainPairView.as_view(), name="login"),
    path("me/", MeView.as_view(), name="me"),
    path("kakao/", KakaoLoginView.as_view(), name="kakao-login"),
    path('achievements/', AchievementListView.as_view()),
    path('ranking/', RankingListView.as_view(), name='ranking'),
    path('titles/', MyTitleListView.as_view()),
]