from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import AllowAny
from django.utils import timezone
from datetime import timedelta
import logging
logger = logging.getLogger(__name__)

from .models import (
    Exercise, ExerciseLog, 
    RunningSession, Quest, UserQuestProgress
)
from .serializers import (
    ExerciseSerializer, ExerciseLogSerializer,
    RunningSessionSerializer, QuestSerializer, UserQuestProgressSerializer
)


# ==========================================
# 1. 일반 운동 및 스트레칭 
# ==========================================
class ExerciseListAPIView(generics.ListAPIView):
    """제공되는 모든 운동/스트레칭 목록 조회"""
    queryset = Exercise.objects.all()
    serializer_class = ExerciseSerializer
    permission_classes = [permissions.AllowAny]


class ExerciseLogListCreateView(generics.ListCreateAPIView):
    """내 운동 기록 조회 및 새 운동 기록 생성"""
    serializer_class = ExerciseLogSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        # 내 기록만 최신순으로 조회
        return ExerciseLog.objects.filter(user=self.request.user).order_by('-created_at')

    def perform_create(self, serializer):
        # 유저 정보를 자동으로 넣어서 저장
        serializer.save(user=self.request.user)


# ==========================================
# 2. 러닝 기록 및 통계
# ==========================================
class RunningSessionListCreateView(generics.ListCreateAPIView):
    """러닝 기록 목록 조회 및 생성"""
    serializer_class = RunningSessionSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user).order_by("-created_at")

    def perform_create(self, serializer):
        logger.debug(f"✅ [RunningSession] 프론트가 보낸 데이터: {self.request.data}")
        serializer.save(user=self.request.user)


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
            sessions = RunningSession.objects.filter(
                user=user,
                start_time__date=target_date
            )
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
    queryset = Exercise.objects.all()
    serializer_class = ExerciseSerializer
    #permission_classes = [permissions.AllowAny] # 기존 코드를 주석 처리
    permission_classes = [permissions.AllowAny] # 누구나 접근 가능하게 변경 (테스트용)
  
        #-------------로그 찍기
    def get(self, request, *args, **kwargs):
        # 터미널에 무조건 찍히는 print 사용
        print("🚀 [SUCCESS] 누군가 운동 목록을 요청했습니다!")
        return super().get(request, *args, **kwargs)
    

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

        # 평균 페이스 계산 (분/km)
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

        return Response({"detail": "Health Connect data uploaded successfully", "id": session.id}, status=201)


# ==========================================
# 4. 퀘스트 시스템 
# ==========================================
class AvailableQuestListAPIView(generics.ListAPIView):
    """현재 활성화된 전체 퀘스트 목록"""
    queryset = Quest.objects.filter(is_active=True)
    serializer_class = QuestSerializer
    permission_classes = [permissions.AllowAny]


class UserQuestProgressListAPIView(generics.ListAPIView):
    """내 퀘스트 진행도 확인"""
    serializer_class = UserQuestProgressSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        return UserQuestProgress.objects.filter(user=self.request.user).order_by("-updated_at")


class ClaimQuestRewardAPIView(APIView):
    """퀘스트 달성 보상 수령"""
    permission_classes = [permissions.AllowAny]

    def post(self, request, pk):
        try:
            progress = UserQuestProgress.objects.get(id=pk, user=request.user)
        except UserQuestProgress.DoesNotExist:
            return Response({"detail": "존재하지 않거나 권한이 없는 퀘스트입니다."}, status=status.HTTP_404_NOT_FOUND)

        if not progress.is_completed:
            return Response({"detail": "아직 완료되지 않은 퀘스트입니다."}, status=status.HTTP_400_BAD_REQUEST)
        
        if progress.completed_at is not None:
            return Response({"detail": "이미 보상을 수령했습니다."}, status=status.HTTP_400_BAD_REQUEST)

        # 보상 지급 및 수령 시간 기록
        user = request.user
        quest = progress.quest
        
        user.exp += quest.reward_xp
        user.point += quest.reward_points
        user.save()

        progress.completed_at = timezone.now()
        progress.save()

        return Response({
            "detail": "보상 수령 완료!",
            "reward_xp": quest.reward_xp,
            "reward_points": quest.reward_points,
            "total_exp": user.exp,
            "total_point": user.point
        }, status=status.HTTP_200_OK)
    
