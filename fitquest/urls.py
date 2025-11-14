from django.contrib import admin
from django.urls import path, include
from rest_framework import routers
from workout.views import RunningSessionViewSet

router = routers.DefaultRouter()
router.register(r'running', RunningSessionViewSet)

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include(router.urls)),
]
