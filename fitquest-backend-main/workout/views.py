from rest_framework import generics, permissions
from rest_framework.views import APIView
from rest_framework.response import Response
from django.utils import timezone
from datetime import datetime, timedelta
from rest_framework.permissions import IsAuthenticated

from .models import RunningSession, Quest, UserQuestProgress
from .serializers import (
    RunningSessionSerializer,
    QuestSerializer,
    UserQuestProgressSerializer
)


# ===============================
# 🔹 RUNNING SESSION 기능
# ===============================

class RunningSummaryTodayView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.now()
        start = now.replace(hour=0, minute=0, second=0, microsecond=0)

        sessions = RunningSession.objects.filter(user=user, start_time__gte=start)

        total_distance = sum(float(s.distance_km) for s in sessions)
        total_duration = sum(s.duration_sec for s in sessions)
        total_calories = sum(float(s.calories_burned or 0) for s in sessions)

        return Response({
            "date": str(start.date()),
            "total_distance_km": total_distance,
            "total_duration_sec": total_duration,
            "total_calories": total_calories,
            "count": sessions.count(),
        })


class RunningSummaryWeekView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.now()
        start = now - timedelta(days=now.weekday())
        start = start.replace(hour=0, minute=0, second=0, microsecond=0)

        sessions = RunningSession.objects.filter(user=user, start_time__gte=start)

        total_distance = sum(float(s.distance_km) for s in sessions)
        total_duration = sum(s.duration_sec for s in sessions)
        total_calories = sum(float(s.calories_burned or 0) for s in sessions)

        return Response({
            "week_start": str(start.date()),
            "total_distance_km": total_distance,
            "total_duration_sec": total_duration,
            "total_calories": total_calories,
            "count": sessions.count(),
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

            sessions = RunningSession.objects.filter(
                user=user,
                start_time__gte=start,
                start_time__lt=end
            )

            distance_sum = sum(float(s.distance_km) for s in sessions)
            data.append({
                "date": str(day),
                "distance": distance_sum
            })

        return Response(list(reversed(data)))


class RunningSessionListCreateView(generics.ListCreateAPIView):
    serializer_class = RunningSessionSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user).order_by("-created_at")

    def perform_create(self, serializer):
        session = serializer.save(user=self.request.user)
        user = self.request.user
        profile = user.profile

        distance = float(session.distance_km)
        gained_exp = int(distance * 10)
        gained_points = int(distance * 5)

        profile.exp += gained_exp
        profile.points += gained_points

        while profile.exp >= (profile.level * 50 + 50):
            profile.exp -= (profile.level * 50 + 50)
            profile.level += 1

        profile.save()


class RunningSessionDetailView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = RunningSessionSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user)


class RunningStatsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        sessions = RunningSession.objects.filter(user=user)

        total_distance = sum(float(s.distance_km) for s in sessions)
        total_duration = sum(s.duration_sec for s in sessions)
        total_calories = sum(float(s.calories_burned or 0) for s in sessions)

        return Response({
            "level": user.profile.level,
            "exp": user.profile.exp,
            "points": user.profile.points,
            "total_distance_km": total_distance,
            "total_duration_sec": total_duration,
            "total_calories": total_calories,
            "session_count": sessions.count(),
        })


# ===============================
# 🔹 QUEST 기능
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
            progress = UserQuestProgress.objects.get(id=pk, user=request.user)
        except UserQuestProgress.DoesNotExist:
            return Response({"detail": "Quest not found or not assigned."}, status=404)

        # 이미 받았는지 체크
        if progress.is_completed and progress.completed_at is not None:
            return Response({"detail": "Reward already claimed."}, status=400)

        # 완료되지 않았다면 보상 지급 불가
        if not progress.is_completed:
            return Response({"detail": "Quest not completed yet."}, status=400)

        # 보상 지급 처리
        progress.completed_at = timezone.now()
        progress.user.exp += progress.quest.reward_xp
        progress.user.point += progress.quest.reward_points
        progress.user.save()
        progress.save()

        return Response({
            "message": "Reward claimed!",
            "xp_gained": progress.quest.reward_xp,
            "points_gained": progress.quest.reward_points,
            "total_user_exp": progress.user.exp,
            "total_user_points": progress.user.point
        }, status=200)