from rest_framework import viewsets
from .models import RunningSession
from .serializers import RunningSessionSerializer
from rest_framework.permissions import AllowAny

class RunningSessionViewSet(viewsets.ModelViewSet):
    queryset = RunningSession.objects.all()
    serializer_class = RunningSessionSerializer
    permission_classes = [AllowAny]
