# fitquest/views.py
import requests
from django.contrib.auth import get_user_model
from django.db import transaction
from rest_framework import permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework_simplejwt.views import TokenObtainPairView
from rest_framework.parsers import JSONParser, FormParser, MultiPartParser  # ★ JSON 파서

from .serializers import (
    SignupSerializer,
    UserSerializer,
    EmailTokenObtainPairSerializer,
    issue_tokens_for_user,
)

User = get_user_model()


# 1) 이메일/비번 로그인 (email + password) → JWT
class EmailTokenObtainPairView(TokenObtainPairView):
    serializer_class = EmailTokenObtainPairSerializer
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]  # 로그인은 JSON만 받도록


# 2) 회원가입 → 유저 생성 + JWT 발급
class SignupView(APIView):
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser, FormParser, MultiPartParser]  # ★ 중요

    @transaction.atomic
    def post(self, request):
        # 디버깅이 필요하면 다음 줄 주석 해제
        # print("REQ DATA (signup):", request.data)

        ser = SignupSerializer(data=request.data)
        ser.is_valid(raise_exception=True)
        user = ser.save()
        tokens = issue_tokens_for_user(user)
        return Response(
            {"user": UserSerializer(user).data, "tokens": tokens},
            status=status.HTTP_201_CREATED,
        )


# 3) 내 정보
class MeView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [JSONParser]

    def get(self, request):
        return Response(UserSerializer(request.user).data)


# 4) 카카오 로그인 (access_token 검증 → 유저 생성/조회 → JWT)
class KakaoLoginView(APIView):
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]

    def post(self, request):
        # print("REQ DATA (kakao):", request.data)
        access_token = request.data.get("access_token")
        if not access_token:
            return Response({"detail": "access_token required"}, status=400)

        try:
            resp = requests.get(
                "https://kapi.kakao.com/v2/user/me",
                headers={"Authorization": f"Bearer {access_token}"},
                timeout=5,
            )
        except Exception as e:
            return Response({"detail": f"kakao request error: {e}"}, status=502)

        if resp.status_code != 200:
            return Response({"detail": "Invalid kakao token"}, status=401)

        data = resp.json()
        kakao_id = str(data.get("id"))
        kakao_email = (data.get("kakao_account") or {}).get("email")

        # 이메일 권한 없으면 대체 이메일 사용
        if not kakao_email:
            kakao_email = f"{kakao_id}@kakao.local"

        with transaction.atomic():
            user, _ = User.objects.get_or_create(
                email=kakao_email,
                defaults={"nickname": f"user_{kakao_id}"},
            )

        tokens = issue_tokens_for_user(user)
        return Response({"user": UserSerializer(user).data, "tokens": tokens}, status=200)
