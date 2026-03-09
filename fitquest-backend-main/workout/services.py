
from django.utils import timezone
from django.db import transaction
from .models import Quest, UserQuestProgress

def get_cycle_key(quest_type):
    """퀘스트 타입에 따라 현재 주기의 키(ID)를 생성"""
    today = timezone.now().date()
    if quest_type == 'daily':
        return str(today)  # "2026-01-06" (오늘 날짜)
    elif quest_type == 'weekly':
        # %Y-W%U : 2025년 2번째 주 -> "2025-W02"
        return today.strftime("%Y-W%U")
    elif quest_type == 'monthly':
        return today.strftime("%Y-%m")
    return "infinite"  # 기간 제한 없는 일반 퀘스트

@transaction.atomic
def process_running_log(running_session):
    """
    러닝 기록이 들어오면 -> 퀘스트 타입(거리, 시간, 칼로리)에 맞춰 진행도 갱신
    """
    user = running_session.user
    
    # 기록에서 필요한 값들 미리 추출
    record_distance = float(running_session.distance_km)
    record_duration_min = running_session.duration_sec / 60.0  # 분 단위 변환
    record_calories = running_session.calories_burned

    active_quests = Quest.objects.filter(is_active=True)

    for quest in active_quests:
        key = get_cycle_key(quest.quest_type)
        
        progress, created = UserQuestProgress.objects.get_or_create(
            user=user,
            quest=quest,
            cycle_key=key,
            defaults={'progress_value': 0, 'is_completed': False}
        )

        # 이미 완료된 퀘스트는 계산 건너뜀 (성능 최적화)
        if progress.is_completed:
            continue

        # ★ 핵심: 퀘스트 측정 기준(metric)에 따라 알맞은 값을 더함
        # (이 부분이 없으면 시간/칼로리 퀘스트가 작동 안 함)
        if quest.metric == 'distance':
            progress.progress_value += record_distance
        elif quest.metric == 'duration':
            progress.progress_value += record_duration_min
        elif quest.metric == 'calories':
            progress.progress_value += record_calories
        
        # 목표 달성 체크
        if progress.progress_value >= quest.target_value:
            progress.is_completed = True
            progress.progress_value = quest.target_value # 100% 채운걸로 표기 (UI 깔끔하게)
            
        progress.save()
    

from rest_framework.exceptions import ValidationError

@transaction.atomic
def claim_reward_service(user, progress_id):
    """
    보상 수령 처리 (트랜잭션 + Row Lock 적용)
    """
    try:
        # select_for_update(): 동시성 제어 (누가 처리 중이면 대기시킴)
        progress = UserQuestProgress.objects.select_for_update().get(
            id=progress_id, 
            user=user
        )
    except UserQuestProgress.DoesNotExist:
        raise ValidationError("존재하지 않거나 권한이 없는 퀘스트입니다.")

    # 1. 검증 로직
    if not progress.is_completed:
        raise ValidationError("아직 완료되지 않은 퀘스트입니다.")
    
    if progress.completed_at is not None:
        raise ValidationError("이미 보상을 수령했습니다.")

    # 2. 보상 지급 (User 모델 업데이트)
    # 퀘스트 정보를 가져오기 위해 select_related 사용 권장 (성능 최적화)
    quest = progress.quest
    
    user.exp += quest.reward_xp
    user.point += quest.reward_points
    user.save()

    # 3. 수령 상태 업데이트
    progress.completed_at = timezone.now()
    progress.save()

    return {
        "message": "보상을 수령했습니다!",
        "current_exp": user.exp,
        "current_point": user.point,
        "gained_xp": quest.reward_xp,
        "gained_point": quest.reward_points
    }