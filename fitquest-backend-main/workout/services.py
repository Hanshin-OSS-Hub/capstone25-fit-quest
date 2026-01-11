
from django.utils import timezone
from django.db import transaction
from .models import Quest, UserQuestProgress

def get_cycle_key(quest_type):
    """퀘스트 타입에 따라 현재 주기의 키(ID)를 생성"""
    today = timezone.now().date()
    if quest_type == 'daily':
        return str(today)  # "2025-01-06" (오늘 날짜)
    elif quest_type == 'weekly':
        # %Y-W%U : 2025년 2번째 주 -> "2025-W02"
        return today.strftime("%Y-W%U")
    elif quest_type == 'monthly':
        return today.strftime("%Y-%m")
    return "infinite"  # 기간 제한 없는 일반 퀘스트

@transaction.atomic
def process_running_log(running_session):
    """
    러닝 기록이 들어오면 -> 해당하는 주기(cycle)의 퀘스트 진행도 갱신
    """
    user = running_session.user
    distance = float(running_session.distance_km)
    
    # 활성화된 모든 퀘스트 가져오기
    active_quests = Quest.objects.filter(is_active=True)

    for quest in active_quests:
        # 1. 지금 달리는 이 기록이 "어느 주기"에 해당하는지 키 생성
        key = get_cycle_key(quest.quest_type)
        
        # 2. "해당 주기"의 진행 데이터가 없으면 새로 만듦 (이게 리셋 효과!)
        progress, created = UserQuestProgress.objects.get_or_create(
            user=user,
            quest=quest,
            cycle_key=key,
            defaults={'progress_value': 0, 'is_completed': False}
        )

        # 3. 완료 안 된 것만 진행도 추가
        if not progress.is_completed:
            progress.progress_value += distance
            
            # 목표 달성 체크
            if progress.progress_value >= quest.target_value:
                progress.is_completed = True
                # 주의: 여기서 보상(exp)을 바로 주지 않습니다. 
                # 나중에 사용자가 "보상 받기" 버튼 누를 때 줍니다. (Claim API)
            
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