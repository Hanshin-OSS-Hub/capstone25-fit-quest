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

# ---------------------------------------------------------------------
# 공통 설정
# ---------------------------------------------------------------------
KAKAO_ME_URL = "https://kapi.kakao.com/v2/user/me"
# SocialAccount.provider 값 (모델에 상수가 있으면 사용, 없으면 'kakao')
PROVIDER_KAKAO = getattr(SocialAccount, "PROVIDER_KAKAO", "kakao")

# SocialAccount에서 "카카오 사용자 고유 id"를 담는 칼럼명을 자동 탐색
# (프로젝트마다 external_id/provider_user_id/provider_id/uid 등 다를 수 있어 방어적으로 처리)
def _detect_social_id_field() -> str:
    candidates = ("provider_user_id", "external_id", "provider_id", "uid")
    for name in candidates:
        try:
            SocialAccount._meta.get_field(name)
            return name
        except Exception:
            continue
    raise RuntimeError(
        "SocialAccount 모델에서 카카오 사용자 ID를 저장할 필드명을 찾지 못했습니다. "
        "예: provider_user_id / external_id / provider_id / uid 중 하나를 모델에 정의하세요."
    )

SOCIAL_ID_FIELD = _detect_social_id_field()


# ---------------------------------------------------------------------
# 유저/소셜 매핑 upsert
# ---------------------------------------------------------------------
def upsert_user_from_kakao_payload(payload: dict) -> User:
    """
    카카오 /v2/user/me 응답(payload)로부터
    - User 생성/조회
    - SocialAccount(provider='kakao', <id_field>=kakao_id) 생성/조회
    를 원자적으로 처리한다.
    """
    kakao_id = str(payload.get("id"))
    account = payload.get("kakao_account") or {}
    profile = account.get("profile") or {}

    kakao_email = account.get("email")  # 동의 안 하면 None
    nickname = profile.get("nickname") or f"user_{kakao_id}"

    with transaction.atomic():
        # 1) 소셜 매핑이 이미 있으면 그 유저를 사용
        filter_kwargs = {"provider": PROVIDER_KAKAO, SOCIAL_ID_FIELD: kakao_id}
        try:
            sa = SocialAccount.objects.select_related("user").get(**filter_kwargs)
            user = sa.user
        except SocialAccount.DoesNotExist:
            # 2) 매핑이 없으면 email 우선으로 존재 유저를 찾고, 없으면 생성
            if kakao_email:
                user, _ = User.objects.get_or_create(
                    email=kakao_email, defaults={"nickname": nickname}
                )
            else:
                user, _ = User.objects.get_or_create(
                    email=f"{kakao_id}@kakao.local",  # 대체 이메일
                    defaults={"nickname": nickname},
                )

            # 3) 소셜 매핑 생성 (email/nickname 칼럼이 모델에 있으면 defaults 로 저장)
            defaults = {"user": user}
            for opt_field, value in (("email", kakao_email), ("nickname", nickname)):
                try:
                    SocialAccount._meta.get_field(opt_field)
                    defaults[opt_field] = value
                except Exception:
                    pass

            create_kwargs = {"provider": PROVIDER_KAKAO, **{SOCIAL_ID_FIELD: kakao_id}}
            SocialAccount.objects.get_or_create(defaults=defaults, **create_kwargs)

    return user


# ---------------------------------------------------------------------
# 1) 이메일/비번 로그인 → JWT
# ---------------------------------------------------------------------
class EmailTokenObtainPairView(TokenObtainPairView):
    serializer_class = EmailTokenObtainPairSerializer
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]  # 로그인은 JSON만 받음


# ---------------------------------------------------------------------
# 2) 회원가입 → 유저 생성 + JWT 발급
# ---------------------------------------------------------------------
class SignupView(APIView):
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]

    @transaction.atomic
    def post(self, request):
        ser = SignupSerializer(data=request.data)
        ser.is_valid(raise_exception=True)
        user = ser.save()
        tokens = issue_tokens_for_user(user)
        return Response({"user": UserSerializer(user).data, "tokens": tokens},
                        status=status.HTTP_201_CREATED)


# ---------------------------------------------------------------------
# 3) 내 정보
# ---------------------------------------------------------------------
class MeView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [JSONParser]

    def get(self, request):
        return Response(UserSerializer(request.user).data)


# ---------------------------------------------------------------------
# 4) 카카오 로그인 (SDK에서 받은 access_token → 검증 → 유저 upsert → JWT)
# ---------------------------------------------------------------------
class KakaoLoginView(APIView):
    permission_classes = [permissions.AllowAny]
    parser_classes = [JSONParser]  # RawPostDataException 방지: JSON만 수신

    def post(self, request):
        access_token = request.data.get("access_token")
        if not access_token:
            return Response({"detail": "access_token required"}, status=400)

        # 카카오 토큰 검증 및 프로필 조회
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
            # 카카오 측 에러를 그대로 보여주면 디버깅에 좋음
            try:
                payload = resp.json()
            except Exception:
                payload = resp.text
            return Response({"detail": "kakao error", "kakao": payload},
                            status=502)

        payload = resp.json()

        # 유저/소셜 매핑 upsert
        try:
            user = upsert_user_from_kakao_payload(payload)
        except Exception as e:
            # 모델 필드 불일치 등 서버 쪽 오류를 친절히 알려주기
            return Response(
                {"detail": "server error while linking social account",
                 "hint": str(e)},
                status=500,
            )

        tokens = issue_tokens_for_user(user)
        return Response({"user": UserSerializer(user).data, "tokens": tokens}, status=200)

