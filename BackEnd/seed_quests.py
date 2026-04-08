from workout.models import Quest

quests = [
    # [제목, 설명, 타입, 측정기준, 목표치, 경험치, 포인트]
    ('기초 체력 다지기', '오늘 1km만 가볍게 달려보세요.', 'daily', 'distance', 1.0, 50, 100),
    ('지구력 테스트', '3km 달리기에 도전하세요!', 'daily', 'distance', 3.0, 150, 300),
    ('지방 연소 작전', '러닝으로 200kcal 소모하기', 'daily', 'calories', 200, 100, 200),
    ('끈기 있는 러너', '오늘 총 30분 이상 운동하세요.', 'daily', 'duration', 1800, 120, 250),
    ('주간 거리 사냥꾼', '이번 주 총 10km를 달성하세요.', 'weekly', 'distance', 10.0, 500, 1000),
    ('칼로리 파괴자', '이번 주 총 1000kcal 소모하기', 'weekly', 'calories', 1000, 400, 800),
    ('시간의 마법사', '이번 주 누적 운동 시간 3시간 달성', 'weekly', 'duration', 10800, 450, 900),
    ('먼 곳을 향해', '오늘 5km 장거리 러닝 도전', 'daily', 'distance', 5.0, 250, 500),
    ('에너지 뿜뿜', '러닝으로 500kcal 소모하기', 'daily', 'calories', 500, 200, 400),
    ('꾸준함의 상징', '오늘 1시간 채워 운동하기', 'daily', 'duration', 3600, 200, 400),
    ('마라톤 준비생', '이번 주 21km 누적 달성', 'weekly', 'distance', 21.0, 1000, 2000),
    ('땀은 배신하지 않는다', '이번 주 2000kcal 소모', 'weekly', 'calories', 2000, 800, 1500),
    ('열정의 화신', '오늘 7km 달리기', 'daily', 'distance', 7.0, 350, 700),
    ('가벼운 발걸음', '오늘 500m 산책하듯 뛰기', 'daily', 'distance', 0.5, 30, 50),
    ('운동 중독자', '오늘 누적 90분 운동하기', 'daily', 'duration', 5400, 300, 600),
    ('주말의 전사', '이번 주 15km 달성하기', 'weekly', 'distance', 15.0, 700, 1400),
    ('월간 목표 달성', '이번 달 총 50km 달리기', 'monthly', 'distance', 50.0, 2000, 5000),
    ('거구의 습격', '오늘 800kcal 소모하기', 'daily', 'calories', 800, 400, 800),
    ('번개 러너', '짧고 굵게! 2km 전력질주', 'daily', 'distance', 2.0, 100, 200),
    ('철인 28호', '이번 달 누적 운동 20시간 달성', 'monthly', 'duration', 72000, 3000, 6000),

    ('초보 러너의 첫걸음', '오늘 1km만 가볍게 달려보세요.', 'daily', 'distance', 1.0, 50, 100),
    ('30분의 기적', '오늘 30분 동안 꾸준히 달려보세요.', 'daily', 'duration', 30.0, 100, 200),
    ('중급자로 가는 길', '오늘 5km 달리기에 도전!', 'daily', 'distance', 5.0, 200, 400),
    ('땀은 배신하지 않는다', '오늘 500kcal 소모하기', 'daily', 'calories', 500, 250, 500),
    ('한 시간의 열정', '60분 동안 멈추지 않고 달리기', 'daily', 'duration', 60.0, 300, 600),
    ('주간 거리 사냥꾼', '이번 주 총 15km 달성하기', 'weekly', 'distance', 15.0, 500, 1000),
    ('일주일의 성실함', '이번 주 누적 150분 운동하기', 'weekly', 'duration', 150.0, 600, 1200),
    ('칼로리 파괴자', '이번 주 총 2000kcal 소모하기', 'weekly', 'calories', 2000, 800, 1500),
    ('모닝 러닝의 즐거움', '가볍게 2km 뛰며 아침 열기', 'daily', 'distance', 2.0, 80, 160),
    ('장거리 마스터', '오늘 10km 장거리 러닝 성공하기', 'daily', 'distance', 10.0, 500, 1000),
    ('끈기 테스트', '90분간 저강도 유산소 운동', 'daily', 'duration', 90.0, 400, 800),
    ('고강도 인터벌', '러닝으로 800kcal 태우기', 'daily', 'calories', 800, 500, 1000),
    ('주말 전사', '이번 주말 동안 10km 달리기', 'weekly', 'distance', 10.0, 400, 800),
    ('월간 목표 달성', '이번 달 총 50km 달리기', 'monthly', 'distance', 50.0, 2000, 5000),
    ('체력왕 도전', '이번 달 누적 500분 운동하기', 'monthly', 'duration', 500.0, 2500, 6000),
    ('대식가 탈출', '오늘 1000kcal 소모하기', 'daily', 'calories', 1000, 600, 1200),
    ('동네 한바퀴', '부담 없이 1.5km 산책하듯 뛰기', 'daily', 'distance', 1.5, 60, 120),
    ('꾸준함이 답이다', '3일 연속 3km 달리기 (진행 중)', 'weekly', 'distance', 9.0, 300, 600),
    ('철인 준비생', '이번 주 30km 달성하기', 'weekly', 'distance', 30.0, 1200, 2500),

    ('지방 연소 작전 II', '러닝으로 200kcal 소모하기 (심화)', 'daily', 'calories', 200, 80, 150),
]

for title, desc, q_type, metric, goal, xp, point in quests:
    Quest.objects.update_or_create(
        title=title,
        defaults={
            "description": desc,
            "quest_type": q_type,
            "metric": metric,
            "target_value": goal,
            "reward_xp": xp,
            "reward_points": point,
            "is_active": True,
        }
    )

print("✅ Quest seed 완료")