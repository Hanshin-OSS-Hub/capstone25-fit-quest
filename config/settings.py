from pathlib import Path
from datetime import timedelta
import os

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',
        },
    },
    'loggers': {
        'django.request': {
            'handlers': ['console'],
            'level': 'DEBUG',  # 상세한 에러와 요청을 모두 보겠다는 뜻
        },
        'fitquest': {  # 내 앱 이름에 맞게 추가 (앱 이름이 fitquest라면)
            'handlers': ['console'],
            'level': 'DEBUG',
        },
        'workout': {  # 운동 앱 로그도 보기 위해 추가
            'handlers': ['console'],
            'level': 'DEBUG',
        },
    },
}

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = "django-insecure-dev-temp-key"
DEBUG = True


# --------------------------------------------------
# 1. INSTALLED_APPS (필요한 최소 구성)
# --------------------------------------------------
INSTALLED_APPS = [
    # Django 기본
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",

    'django.contrib.staticfiles',

    # REST API 필수
    "rest_framework",
    "rest_framework_simplejwt",
    "django_filters",
    'drf_yasg',

    # 우리 앱
    "fitquest.apps.FitquestConfig",
    'workout.apps.WorkoutConfig',
]

# --------------------------------------------------
# 2. 미들웨어 (CSRF는 안드로이드에서 JWT로 대체)
# --------------------------------------------------
MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
]

# --------------------------------------------------
# 3. 기본 경로 / WSGI
# --------------------------------------------------
ROOT_URLCONF = "config.urls"
WSGI_APPLICATION = "config.wsgi.application"

# --------------------------------------------------
# 4. 데이터베이스 (MySQL)
# --------------------------------------------------

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'fitquest_db',
        'USER': 'user',
        'PASSWORD': 'password123',
        'HOST': '127.0.0.1',
        'PORT': '3306',
    }
}


# --------------------------------------------------
# 5. 사용자 인증 모델 (커스텀 유저)
# --------------------------------------------------
AUTH_USER_MODEL = "fitquest.CustomUser"

# --------------------------------------------------
# 6. DRF + JWT 설정
# --------------------------------------------------
REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": (
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ),
    "DEFAULT_PERMISSION_CLASSES": (
        "rest_framework.permissions.IsAuthenticated",
    ),
    "DEFAULT_PARSER_CLASSES": (
        "rest_framework.parsers.JSONParser",
        "rest_framework.parsers.FormParser",
        "rest_framework.parsers.MultiPartParser",
    ),
}



SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(hours=1),   # Access 토큰 유효시간
    "REFRESH_TOKEN_LIFETIME": timedelta(days=7),   # Refresh 토큰 유효시간
    "ROTATE_REFRESH_TOKENS": True,
    "BLACKLIST_AFTER_ROTATION": False,
    "AUTH_HEADER_TYPES": ("Bearer",),
}
TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],           # 템플릿 폴더 안 쓰면 빈 리스트
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.debug",
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]


# --------------------------------------------------
# 7. 국제화 (한국 기준)
# --------------------------------------------------
LANGUAGE_CODE = "ko-kr"
TIME_ZONE = "Asia/Seoul"
USE_I18N = True
USE_TZ = False

# --------------------------------------------------
# 8. 정적 파일 (거의 안씀)
# --------------------------------------------------
STATIC_URL = "static/"
STATIC_ROOT = BASE_DIR / "staticfiles"

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

# -- CSRF
CSRF_TRUSTED_ORIGINS = [
    "https://fitquest25.xyz",
    "https://www.fitquest25.xyz",
]
SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'https')

ALLOWED_HOSTS = [
    "fitquest25.xyz",
    "www.fitquest25.xyz",
    "localhost",
    "127.0.0.1",
    "0.0.0.0",

]


