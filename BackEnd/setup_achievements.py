from workout.models import Achievement

def setup_monster_achievements():
    achievements = [
        # A. 거리 기반 (metric: total_distance)
        {'name': '첫 발걸음', 'description': '알에서 깨어나 1km를 이동했습니다.', 'metric': 'total_distance', 'target_value': 1.0, 'reward_title': '아장아장 괴수', 'reward_exp': 50},
        {'name': '동네 마실', 'description': '동네 구석구석 50km를 누볐습니다.', 'metric': 'total_distance', 'target_value': 50.0, 'reward_title': '우리동네 고질라', 'reward_exp': 500},
        {'name': '국토 대장정', 'description': '500km 주파! 이제 지구를 정복할 준비가 되었습니다.', 'metric': 'total_distance', 'target_value': 500.0, 'reward_title': '지구 정복자', 'reward_exp': 2000},

        # B. 칼로리 기반 (metric: total_calories)
        {'name': '지방 소각', 'description': '10,000 kcal를 태워 몸에서 열이 납니다.', 'metric': 'total_calories', 'target_value': 10000.0, 'reward_title': '불타는 뱃살', 'reward_exp': 300},
        {'name': '용광로 신체', 'description': '100,000 kcal 돌파! 온몸이 이글거립니다.', 'metric': 'total_calories', 'target_value': 100000.0, 'reward_title': '지옥의 파이어볼', 'reward_exp': 1500},
        
        # C. 꾸준함 기반 (하찮지만 끈질긴 괴수) 
        {'name': '질긴 생명력', 'description': '누적 운동 시간 3일 달성! 하찮지만 끈질깁니다.', 'metric': 'total_duration', 'target_value': 259200.0, 'reward_title': '끈질긴 젤리발바닥', 'reward_exp': 500},
        {'name': '끈기의 화신', 'description': '누적 운동 시간 30일 돌파! 당신은 불사신입니다.', 'metric': 'total_duration', 'target_value': 2592000.0, 'reward_title': '불사신 고무찰흙', 'reward_exp': 3000},
    ]

    for data in achievements:
        Achievement.objects.get_or_create(
            name=data['name'],
            defaults=data
        )
    print("✅ 귀여운 괴수 업적 데이터 생성 완료!")

# 실행
setup_monster_achievements()