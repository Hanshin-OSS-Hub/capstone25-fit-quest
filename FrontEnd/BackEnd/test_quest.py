from workout.models import RunningSession, UserQuestProgress, Quest
from workout.serializers import process_running_log
from django.contrib.auth import get_user_model
from django.utils import timezone
from datetime import date

User = get_user_model()
try:
    # 1. 유저 확인 및 생성 (username 제외)
    user = User.objects.first()
    if not user:
        user = User.objects.create_user(
            email='test@example.com', 
            nickname='테스터', 
            password='password123'
        )
        print('👤 새 유저 생성 완료')
    else:
        print(f'👤 기존 유저 사용: {user.email}')

    # 2. 오늘자 퀘스트 할당
    today_key = date.today().strftime('%Y-%m-%d')
    uq, created = UserQuestProgress.objects.get_or_create(
        user=user, 
        cycle_key=today_key,
        quest=Quest.objects.filter(quest_type='daily').first()
    )
    print(f'📜 테스트 퀘스트: {uq.quest.title if uq.quest else "없음"}')

    # 3. 5km 달리기 기록 생성
    mock_session = RunningSession.objects.create(
        user=user,
        distance_km=5.0,
        duration_sec=1800,
        start_time=timezone.now(),
        end_time=timezone.now(),
        calories_burned=350
    )
    print('🏃 5km 운동 기록 생성')

    # 4. 로직 실행 및 결과 출력
    process_running_log(mock_session)
    uq.refresh_from_db()
    status = '완료' if uq.is_completed else '진행 중'
    print(f'✅ 결과: {uq.progress_value}/{uq.quest.target_value} ({status})')

except Exception as e:
    print(f'❌ 에러 발생: {e}')
