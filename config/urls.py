from django.contrib import admin
from django.urls import path, include
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi

# Swagger 설정
schema_view = get_schema_view(
   openapi.Info(
      title="FitQuest API",
      default_version='v1',
      description="캡스톤 디자인 FitQuest 백엔드 API 명세서입니다.",
      contact=openapi.Contact(email="admin@fitquest.com"),
   ),
   public=True,
   permission_classes=(permissions.AllowAny,),
)

urlpatterns = [
    path("admin/", admin.site.urls),
    
    # 앱 URL 연결
    path("api/auth/", include("fitquest.urls")),   # 인증 관련
    path("api/workout/", include("workout.urls")), # 운동/퀘스트 관련

    # Swagger URL (여기로 접속하면 문서가 뜹니다)
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
    path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),
]