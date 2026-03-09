from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from django.utils import timezone
from django.db.models import Sum
from datetime import timedelta, datetime
from rest_framework.permissions import IsAuthenticated
from rest_framework.exceptions import ValidationError

from .models import RunningSession, Quest, UserQuestProgress
from .serializers import (
    RunningSessionSerializer,
    QuestSerializer,
    UserQuestProgressSerializer
)
# ★ 추가: 서비스 레이어 임포트
from .services import claim_reward_service 


# ==========================================
# 1. 러닝 기록 및 통계 (Running Sessions)
# ==========================================

class RunningSessionListCreateView(generics.ListCreateAPIView):
    """러닝 기록 목록 조회 및 생성"""
    serializer_class = RunningSessionSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user).order_by("-created_at")

    def perform_create(self, serializer):
        # 시리얼라이저의 create()에서 페이스/칼로리가 자동 계산됨
        serializer.save(user=self.request.user)


class RunningSessionDetailView(generics.RetrieveUpdateDestroyAPIView):
    """러닝 기록 상세 조회, 수정, 삭제"""
    serializer_class = RunningSessionSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return RunningSession.objects.filter(user=self.request.user)


class RunningSummaryTodayView(APIView):
    """오늘 하루 총 운동 요약"""
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        start = timezone.now().replace(hour=0, minute=0, second=0, microsecond=0)
        
        stats = RunningSession.objects.filter(user=user, start_time__gte=start).aggregate(
            dist=Sum('distance_km'), dur=Sum('duration_sec'), cal=Sum('calories_burned')
        )

        return Response({
            "date": str(start.date()),
            "total_distance_km": stats['dist'] or 0,
            "total_duration_sec": stats['dur'] or 0,
            "total_calories": stats['cal'] or 0,
            "count": RunningSession.objects.filter(user=user, start_time__gte=start).count()
        })


class RunningSummary7DaysView(APIView):
    """최근 7일간 일별 통계 (차트용 데이터)"""
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        today = timezone.now().date()
        data = []

        for i in range(6, -1, -1):
            day = today - timedelta(days=i)
            # 해당 날짜의 00시부터 24시까지
            start = datetime.combine(day, datetime.min.time(), tzinfo=timezone.get_current_timezone())
            end = start + timedelta(days=1)

            daily_dist = RunningSession.objects.filter(
                user=user, start_time__gte=start, start_time__lt=end
            ).aggregate(total=Sum('distance_km'))['total'] or 0

            data.append({
                "date": day.strftime('%m-%d'),
                "distance": float(daily_dist)
            })

        return Response(data)


class RunningStatsView(APIView):
    """전체 누적 통계 및 현재 레벨 정보"""
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        stats = RunningSession.objects.filter(user=user).aggregate(
            dist=Sum('distance_km'), dur=Sum('duration_sec'), cal=Sum('calories_burned')
        )

        return Response({
            "level": user.level, # models.py의 @property 사용
            "exp": user.exp,
            "point": user.point,
            "total_distance_km": stats['dist'] or 0,
            "total_duration_sec": stats['dur'] or 0,
            "total_calories": stats['cal'] or 0,
            "session_count": RunningSession.objects.filter(user=user).count(),
        })


# ==========================================
# 2. 퀘스트 시스템 (Quests)
# ==========================================

class AvailableQuestListAPIView(generics.ListAPIView):
    """전체 퀘스트 목록"""
    queryset = Quest.objects.filter(is_active=True)
    serializer_class = QuestSerializer
    permission_classes = [IsAuthenticated]


class UserQuestProgressListAPIView(generics.ListAPIView):
    """내 퀘스트 진행도 확인"""
    serializer_class = UserQuestProgressSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        # 오늘 날짜를 기준으로 필터링하거나 전체 진행도를 보여줌
        return UserQuestProgress.objects.filter(user=self.request.user).order_by("-updated_at")



class ClaimQuestRewardAPIView(APIView):
    """퀘스트 보상 수령 (서비스 레이어 활용)"""
    permission_classes = [IsAuthenticated]

    def post(self, request, pk):
        try:
            # ★ 수정: 뷰에서 직접 로직을 짜지 않고, 
            # 이전에 만든 전문 서비스(claim_reward_service)를 호출합니다.
            # 이 서비스 안에 '이미 수령했는지', '완료됐는지' 체크 로직이 다 들어있습니다.
            result = claim_reward_service(request.user, pk)
            
            return Response(result, status=status.HTTP_200_OK)
            
        except ValidationError as e:
            # 서비스에서 발생한 에러 메시지를 클라이언트에 전달
            return Response({"detail": e.detail}, status=status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response({"detail": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
