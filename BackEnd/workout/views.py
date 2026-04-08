from requests import session
from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
import random
from django.utils import timezone
from datetime import date, timedelta
import logging

from urllib3 import request


# 로거 설정
logger = logging.getLogger(__name__)

from .models import (
    Exercise, ExerciseLog, RunningSession, Quest,
    UserQuestProgress, Workout, Achievement, UserAchievement
)
from .serializers import (
    ExerciseSerializer, ExerciseLogSerializer, RunningSessionSerializer,
    QuestSerializer, UserQuestProgressSerializer, WorkoutSerializer, AchievementSerializer
)


# ==========================================
# 0. 업적 자동 달성 처리 유틸
# ==========================================
def calculate_user_achievement_metrics(user):
    running_sessions = RunningSession.objects.filter(user=user)

    total_distance = sum(float(s.distance_km or 0) for s in running_sessions)
    total_duration = sum(int(s.duration_sec or 0) for s in running_sessions)
    total_calories = sum(float(s.calories_burned or 0) for s in running_sessions)

    running_dates = set(
        RunningSession.objects.filter(user=user)
        .values_list("start_time__date", flat=True)
    )
    exercise_dates = set(
        ExerciseLog.objects.filter(user=user)
        .values_list("created_at__date", flat=True)
    )
    total_days = len(running_dates | exercise_dates)

    return {
        "total_distance": total_distance,
        "total_duration": total_duration,
        "total_calories": total_calories,
        "total_days": total_days,
    }

def check_and_grant_achievements(user):
    metrics = calculate_user_achievement_metrics(user)
    achievements = Achievement.objects.all()
    newly_achieved = []
    gained_exp = 0

    for achievement in achievements:
        current_value = metrics.get(achievement.metric)

        if current_value is None:
            continue

        if float(current_value) >= float(achievement.target_value):
            _, created = UserAchievement.objects.get_or_create(
                user=user,
                achievement=achievement
            )

            if created:
                newly_achieved.append({
                    "id": achievement.id,
                    "name": achievement.name,
                    "reward_title": achievement.reward_title,
                    "reward_exp": getattr(achievement, "reward_exp", 0),
                })

                gained_exp += int(getattr(achievement, "reward_exp", 0) or 0)

    if gained_exp > 0:
        user.exp += gained_exp
        user.save(update_fields=["exp"])

    return newly_achieved

def get_cycle_key(quest_type):
    today = date.today()

    if quest_type == "daily":
        return today.strftime("%Y-%m-%d")
    elif quest_type == "weekly":
        return today.strftime("%Y-W%U")
    elif quest_type == "monthly":
        return today.strftime("%Y-%m")
    return today.strftime("%Y-%m-%d")


def update_user_quest_progress(user, distance_km=0, duration_sec=0, calories_burned=0):
    cycle_keys = [
        get_cycle_key("daily"),
        get_cycle_key("weekly"),
        get_cycle_key("monthly"),
    ]

    progresses = UserQuestProgress.objects.filter(
        user=user,
        cycle_key__in=cycle_keys
    ).select_related("quest")

    for progress in progresses:
        if progress.is_completed:
            continue

        quest = progress.quest

        if quest.metric == "distance":
            progress.progress_value += float(distance_km or 0)
        elif quest.metric == "duration":
            progress.progress_value += float(duration_sec or 0)
        elif quest.metric == "calories":
            progress.progress_value += float(calories_burned or 0)

        if float(progress.progress_value) >= float(quest.target_value):
            progress.is_completed = True
            if progress.completed_at is None:
                progress.completed_at = timezone.now()

        progress.save()

# ==========================================
# 1. 일반 운동 및 스트레칭
# ==========================================
class ExerciseListAPIView(generics.ListAPIView):
    """제공되는 모든 운동/스트레칭 목록 조회"""
    queryset = Exercise.objects.all()
    serializer_class = ExerciseSerializer
    permission_classes = [permissions.AllowAny]

    def get(self, request, *args, **kwargs):
        print("🚀 [SUCCESS] 누군가 운동 목록을 요청했습니다!")
        return super().get(request, *args, **kwargs)


class ExerciseLogListCreateView(generics.ListCreateAPIView):
    """내 운동 기록 조회 및 새 운동 기록 생성"""
    serializer_class = ExerciseLogSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        return ExerciseLog.objects.filter(user=self.request.user).order_by('-created_at')

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


# ==========================================
# 2. 러닝 기록 및 통계
# ==========================================
class RunningSessionListCreateView(generics.ListCreateAPIView):
    """러닝 기록 목록 조회 및 생성"""
    serializer_class = RunningSessionSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user).order_by("-created_at")

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        logger.debug(f"✅ [RunningSession] 프론트가 보낸 데이터: {self.request.data}")
        session = serializer.save(user=request.user)

        new_distance = float(session.distance_km or 0)
        new_duration = int(session.duration_sec or 0)
        new_calories = int(session.calories_burned or 0)

        update_user_quest_progress(
            request.user,
            distance_km=new_distance,
            duration_sec=new_duration,
            calories_burned=new_calories,
        )

        newly_achieved = check_and_grant_achievements(request.user)

        response_data = serializer.data
        response_data["new_achievements"] = newly_achieved
        response_data["current_exp"] = request.user.exp
        response_data["current_level"] = request.user.level
        headers = self.get_success_headers(serializer.data)

        return Response(response_data, status=status.HTTP_201_CREATED, headers=headers)

class RunningSessionDetailView(generics.RetrieveUpdateDestroyAPIView):
    """러닝 기록 상세 조회, 수정, 삭제"""
    serializer_class = RunningSessionSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user)


class RunningSummaryTodayView(APIView):
    """오늘 하루 총 러닝 요약"""
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        user = request.user
        start = timezone.now().replace(hour=0, minute=0, second=0, microsecond=0)
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


class RunningSummary7DaysView(APIView):
    """최근 7일간 일별 러닝 통계"""
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        user = request.user
        today = timezone.now().date()
        data = []

        for i in range(6, -1, -1):
            target_date = today - timedelta(days=i)
            sessions = RunningSession.objects.filter(user=user, start_time__date=target_date)
            daily_dist = sum(float(s.distance_km) for s in sessions)
            data.append({
                "date": target_date.strftime('%m-%d'),
                "distance": daily_dist
            })
        return Response(data)


class RunningStatsView(APIView):
    """전체 누적 러닝 통계"""
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        user = request.user
        sessions = RunningSession.objects.filter(user=user)

        total_distance = sum(float(s.distance_km) for s in sessions)
        total_duration = sum(s.duration_sec for s in sessions)
        total_calories = sum(float(s.calories_burned or 0) for s in sessions)

        return Response({
            "exp": user.exp,
            "point": user.point,
            "total_distance_km": total_distance,
            "total_duration_sec": total_duration,
            "total_calories": total_calories,
            "session_count": sessions.count(),
        })


# ==========================================
# 3. 헬스커넥트 연동
# ==========================================
class HealthConnectRunningUploadView(APIView):
    """안드로이드 헬스커넥트 데이터 업로드"""
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        logger.debug(f"📡 [HealthConnect] 프론트가 보낸 데이터: {request.data}")
        user = request.user
        distance_km = request.data.get("distance")
        duration_sec = request.data.get("duration")
        start_time = request.data.get("start_time")
        end_time = request.data.get("end_time")
        calories = request.data.get("calories")

        if not distance_km or not duration_sec:
            return Response({"error": "distance_km and duration_sec are required"}, status=400)

        avg_pace = 0.00
        if float(distance_km) > 0:
            avg_pace = (float(duration_sec) / 60) / float(distance_km)

        session = RunningSession.objects.create(
            user=user,
            distance_km=distance_km,
            duration_sec=duration_sec,
            avg_pace_min_per_km=round(avg_pace, 2),
            calories_burned=calories or int(float(distance_km) * 70),
            start_time=start_time,
            end_time=end_time
        )

        update_user_quest_progress(
            user,
            distance_km=float(distance_km or 0),
            duration_sec=int(duration_sec or 0),
            calories_burned=float(calories or 0),
        )

        newly_achieved = check_and_grant_achievements(user)

        return Response({
            "detail": "Health Connect data uploaded successfully",
            "id": session.id,
            "new_achievements": newly_achieved,
            "current_exp": user.exp,
            "current_level": user.level,
        }, status=201)


# ==========================================
# 4. 퀘스트 시스템
# ==========================================
class AvailableQuestListAPIView(generics.ListAPIView):
    """현재 활성화된 전체 퀘스트 목록"""
    queryset = Quest.objects.filter(is_active=True)
    serializer_class = QuestSerializer
    permission_classes = [permissions.AllowAny]

class UserQuestProgressListAPIView(generics.ListAPIView):
    """내 퀘스트 진행도 확인 (없으면 자동 생성, 많으면 정리)"""
    serializer_class = UserQuestProgressSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        all_progress = []

        for quest_type in ["daily", "weekly", "monthly"]:
            cycle_key = get_cycle_key(quest_type)

            existing = list(
                UserQuestProgress.objects.filter(
                    user=user,
                    cycle_key=cycle_key,
                    quest__quest_type=quest_type
                ).select_related("quest").order_by("-id")
            )

            # 잘못 많이 들어간 데이터가 있으면 3개만 남기고 정리
            if len(existing) > 3:
                extra_ids = [p.id for p in existing[3:]]
                UserQuestProgress.objects.filter(id__in=extra_ids).delete()
                existing = existing[:3]

            # 이미 3개가 있으면 그대로 사용
            if len(existing) == 3:
                all_progress.extend(existing)
                continue

            # 1개나 2개만 있는 애매한 상태면 다 지우고 새로 3개 생성
            if 0 < len(existing) < 3:
                UserQuestProgress.objects.filter(
                    user=user,
                    cycle_key=cycle_key,
                    quest__quest_type=quest_type
                ).delete()
                existing = []

            candidates = list(
                Quest.objects.filter(
                    quest_type=quest_type,
                    is_active=True
                )
            )

            if not candidates:
                continue

            selected = random.sample(candidates, min(3, len(candidates)))

            for quest in selected:
                progress = UserQuestProgress.objects.create(
                    user=user,
                    quest=quest,
                    progress_value=0,
                    is_completed=False,
                    is_reward_claimed=False,
                    cycle_key=cycle_key
                )
                all_progress.append(progress)

        return sorted(all_progress, key=lambda x: x.id, reverse=True)

class ClaimQuestRewardAPIView(APIView):
    """퀘스트 달성 보상 수령"""
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, pk):
        try:
            progress = UserQuestProgress.objects.get(id=pk, user=request.user)
        except UserQuestProgress.DoesNotExist:
            return Response(
                {"detail": "존재하지 않거나 권한이 없는 퀘스트입니다."},
                status=status.HTTP_404_NOT_FOUND
            )

        if not progress.is_completed:
            return Response(
                {"detail": "아직 완료되지 않은 퀘스트입니다."},
                status=status.HTTP_400_BAD_REQUEST
            )

        if progress.is_reward_claimed:
            return Response(
                {"detail": "이미 보상을 수령했습니다."},
                status=status.HTTP_400_BAD_REQUEST
            )

        user = request.user
        quest = progress.quest

        user.exp += quest.reward_xp
        user.point += quest.reward_points
        user.save()

        progress.is_reward_claimed = True
        progress.save(update_fields=["is_reward_claimed"])

        return Response({
            "detail": "보상 수령 완료!",
            "reward_xp": quest.reward_xp,
            "reward_points": quest.reward_points,
            "total_exp": user.exp,
            "total_point": user.point,
            "completed_at": progress.completed_at,
        }, status=status.HTTP_200_OK)


# ==========================================
# 5. 홈 트레이닝 도감
# ==========================================
class WorkoutListView(APIView):
    """운동 도감 목록 조회 (필터 포함)"""
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        workouts = Workout.objects.all()
        level = request.query_params.get('level')
        category = request.query_params.get('category')

        if level:
            workouts = workouts.filter(level=level)
        if category:
            workouts = workouts.filter(category=category)

        serializer = WorkoutSerializer(workouts, many=True)
        return Response(serializer.data)


# ==========================================
# 6. 업적 및 칭호
# ==========================================
class AchievementListAPIView(generics.ListAPIView):
    """업적 목록 조회"""
    queryset = Achievement.objects.all().order_by("id")
    serializer_class = AchievementSerializer
    permission_classes = [permissions.IsAuthenticated]