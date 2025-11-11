from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import SignupView, EmailTokenObtainPairView, MeView, KakaoLoginView

urlpatterns = [
    # 회원가입 / 로그인 / 토큰 갱신
    path("auth/signup/", SignupView.as_view(), name="signup"),
    path("auth/login/", EmailTokenObtainPairView.as_view(), name="login"),   # email+password
    path("auth/refresh/", TokenRefreshView.as_view(), name="token-refresh"),

    # 내 정보
    path("auth/me/", MeView.as_view(), name="me"),

    # 카카오 로그인
    path("auth/kakao/", KakaoLoginView.as_view(), name="kakao-login"),
]
