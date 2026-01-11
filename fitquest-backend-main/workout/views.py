from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from django.utils import timezone
from datetime import datetime, timedelta
from rest_framework.permissions import IsAuthenticated
from rest_framework.exceptions import ValidationError
from django.db.models import Sum

# 모델 임포트 (Exercise 제거함)
from .models import RunningSession, Quest, UserQuestProgress
from .services import claim_reward_service

# 시리얼라이저 임포트 (Exercise 제거함)
from .serializers import (
    RunningSessionSerializer,
    QuestSerializer,
    UserQuestProgressSerializer
)


# ===============================
# 🔹 RUNNING SESSION 기능 (러닝)
# ===============================

class RunningSummaryTodayView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.now()
        start = now.replace(hour=0, minute=0, second=0, microsecond=0)

        stats = RunningSession.objects.filter(user=user, start_time__gte=start).aggregate(
            total_distance=Sum('distance_km'),
            total_duration=Sum('duration_sec'),
            total_calories=Sum('calories_burned')
        )

        return Response({
            "date": str(start.date()),
            "total_distance_km": stats['total_distance'] or 0,
            "total_duration_sec": stats['total_duration'] or 0,
            "total_calories": stats['total_calories'] or 0,
            "count": RunningSession.objects.filter(user=user, start_time__gte=start).count(),
        })


class RunningSummaryWeekView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.now()
        start = now - timedelta(days=now.weekday())
        start = start.replace(hour=0, minute=0, second=0, microsecond=0)

        stats = RunningSession.objects.filter(user=user, start_time__gte=start).aggregate(
            total_distance=Sum('distance_km'),
            total_duration=Sum('duration_sec'),
            total_calories=Sum('calories_burned')
        )

        return Response({
            "week_start": str(start.date()),
            "total_distance_km": stats['total_distance'] or 0,
            "total_duration_sec": stats['total_duration'] or 0,
            "total_calories": stats['total_calories'] or 0,
            "count": RunningSession.objects.filter(user=user, start_time__gte=start).count(),
        })


class RunningSummary7DaysView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        today = timezone.now().date()

        data = []
        for i in range(7):
            day = today - timedelta(days=i)
            start = datetime.combine(day, datetime.min.time(), tzinfo=timezone.get_current_timezone())
            end = start + timedelta(days=1)

            daily_stat = RunningSession.objects.filter(
                user=user,
                start_time__gte=start,
                start_time__lt=end
            ).aggregate(dist=Sum('distance_km'))

            data.append({
                "date": str(day),
                "distance": daily_stat['dist'] or 0
            })

        return Response(list(reversed(data)))


class RunningSessionListCreateView(generics.ListCreateAPIView):
    serializer_class = RunningSessionSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user).order_by("-created_at")

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


class RunningSessionDetailView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = RunningSessionSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user)


class RunningStatsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        stats = RunningSession.objects.filter(user=user).aggregate(
            total_distance=Sum('distance_km'),
            total_duration=Sum('duration_sec'),
            total_calories=Sum('calories_burned')
        )

        return Response({
            "level": getattr(user, 'level', 1),
            "exp": user.exp,
            "points": user.point,
            "total_distance_km": stats['total_distance'] or 0,
            "total_duration_sec": stats['total_duration'] or 0,
            "total_calories": stats['total_calories'] or 0,
            "session_count": RunningSession.objects.filter(user=user).count(),
        })


# ===============================
# 🔹 QUEST 기능 (퀘스트)
# ===============================

class AvailableQuestListAPIView(generics.ListAPIView):
    serializer_class = QuestSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return Quest.objects.filter(is_active=True)


class UserQuestProgressListAPIView(generics.ListAPIView):
    serializer_class = UserQuestProgressSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return UserQuestProgress.objects.filter(user=self.request.user)


class ClaimQuestRewardAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, pk):
        try:
            result = claim_reward_service(request.user, pk)
            return Response(result, status=status.HTTP_200_OK)
        except ValidationError as e:
            msg = e.detail[0] if isinstance(e.detail, list) else str(e.detail)
            return Response({"detail": msg}, status=status.HTTP_400_BAD_REQUEST)