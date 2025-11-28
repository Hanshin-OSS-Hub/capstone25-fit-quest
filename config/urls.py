from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path("admin/", admin.site.urls),
    path("api/", include("fitquest.urls")),   # ★ API prefix
    path('api/', include('workout.urls')),
]