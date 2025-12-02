# fitquest/views.py

import requests
from django.contrib.auth import get_user_model
from django.db import transaction
from rest_framework import permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import JSONParser
from rest_framework_simplejwt.views import TokenObtainPairView

from .serializers import (
    SignupSerializer,
    UserSerializer,
    EmailTokenObtainPairSerializer,
    issue_tokens_for_user,
)
from .models import SocialAccount

User = get_user_model()

# -----------------------------------------------------
# 공통 설정
# -----------------------------------------------------

KAKAO_ME_URL = "https://kapi.kakao.com/v2/user/me"
PROVIDER_KAKAO = SocialAccount.PROVIDER_KAKAO


# -----------------------------------------------------
# 카카오 upsert — (너희 SocialAccount 모델에 100% 맞춘 버전)
# -----------------------------------------------------
def upsert_user_from_kakao_payload(payload: dict) -> User:
    """
    카카오 /v2/user/me 응답(payload) 기반으로
    User + SocialAccount를 안전하게 upsert.
    """
    kakao_id = str(payload.get("id"))
    account = payload.get("kakao_account") or {}
    profile = account.get("profile") or {}

    kakao_email = account.get("email")  # 동의 안 하면 None
    nickname = profile.get("nickname") or f"user_{kakao_id}"

    with transaction.atomic():
        # 1) 이미 소셜 계정 연동된 유저인지 확인
        try:
            sa = SocialAccount.objects.select_related("user").get(
                provider=PROVIDER_KAKAO,
                provider_user_id=kakao_id,
            )
            return sa.user

        except SocialAccount.DoesNotExist:
            # 2) 유저 찾기 or 생성
            if kakao_email:
                user, _ = User.objects.get_or_create(
                    email=kakao_email,
                    defaults={"nickname": nickname},
                )
            else:
                # 이메일 제공 안 했을 때 대체 이메일
                placeholder_email = f"{kakao_id}@kakao.local"
                user, _ = User.objects.get_or_create(
                    email=placeholder_email,
                    defaults={"nickname": nickname},
                )

            # 3) SocialAccount 생성
            SocialAccount.objects.create(
                user=user,
                provider=PROVIDER_KAKAO,
                provider_user_id=kakao_id,
                email=kakao_email,
                nickname=nickname,
            )

            return user


# -----------------------------------------------------
# 1) 이메일/비번 로그인 → JWT 발급
# -----------------------------------------------------
class EmailTokenObtainPairView(TokenObtainPairView):
    serializer_class = EmailTokenObtainPairSerializer
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]


# -----------------------------------------------------
# 2) 회원가입 → 유저 생성 + JWT
# -----------------------------------------------------
class SignupView(APIView):
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]

    @transaction.atomic
    def post(self, request):
        ser = SignupSerializer(data=request.data)
        ser.is_valid(raise_exception=True)
        user = ser.save()
        tokens = issue_tokens_for_user(user)
        return Response(
            {"user": UserSerializer(user).data, "tokens": tokens},
            status=status.HTTP_201_CREATED,
        )


# -----------------------------------------------------
# 3) 내 정보 조회
# -----------------------------------------------------
class MeView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [JSONParser]

    def get(self, request):
        return Response(UserSerializer(request.user).data)


# -----------------------------------------------------
# 4) 카카오 로그인 (access_token → 유저 확인 → JWT)
# -----------------------------------------------------
class KakaoLoginView(APIView):
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]

    def post(self, request):
        access_token = request.data.get("access_token")
        if not access_token:
            return Response({"detail": "access_token required"}, status=400)

        # 카카오 API에 사용자 정보 요청
        try:
            resp = requests.get(
                KAKAO_ME_URL,
                headers={"Authorization": f"Bearer {access_token}"},
                timeout=5,
            )
        except requests.exceptions.RequestException as e:
            return Response({"detail": f"kakao request error: {e}"}, status=502)

        if resp.status_code == 401:
            return Response({"detail": "Invalid kakao token"}, status=401)

        if resp.status_code != 200:
            return Response({"detail": "kakao error", "kakao": resp.json()}, status=502)

        payload = resp.json()

        # 유저 upsert
        try:
            user = upsert_user_from_kakao_payload(payload)
        except Exception as e:
            return Response(
                {
                    "detail": "server error while linking social account",
                    "hint": str(e),
                },
                status=500,
            )

        tokens = issue_tokens_for_user(user)

        return Response(
            {
                "user": UserSerializer(user).data,
                "tokens": tokens,
            },
            status=200,
        )
