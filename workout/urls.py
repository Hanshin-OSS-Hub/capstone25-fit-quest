from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import RunningSessionViewSet

router = DefaultRouter()
router.register(r'running', RunningSessionViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
