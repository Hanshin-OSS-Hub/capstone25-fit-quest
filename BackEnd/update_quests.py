from workout.models import Quest, UserQuestProgress

QUEST_DATA = [
    # DAILY - distance
    ('가벼운 첫걸음 1', '오늘 1km 가볍게 달려보세요.', 'daily', 'distance', 1.0, 20, 40),
    ('가벼운 첫걸음 2', '오늘 1.5km 달려보세요.', 'daily', 'distance', 1.5, 40, 80),
    ('동네 한바퀴', '부담 없이 2km 뛰어보세요.', 'daily', 'distance', 2.0, 60, 120),
    ('번개 러너', '짧고 굵게 2.5km 달리기!', 'daily', 'distance', 2.5, 80, 160),
    ('기초 체력 강화', '오늘 3km 달리기에 도전하세요.', 'daily', 'distance', 3.0, 120, 240),
    ('중급 러너', '오늘 3.5km 달리기에 도전하세요.', 'daily', 'distance', 3.5, 200, 400),
    ('열정 러너', '오늘 4km 장거리 러닝!', 'daily', 'distance', 4.0, 300, 600),
    ('장거리 마스터', '오늘 5km 달리기 성공하기.', 'daily', 'distance', 5.0, 450, 900),

    # DAILY - duration
    ('몸풀기 시간', '오늘 20분 이상 운동하세요.', 'daily', 'duration', 1200, 100, 200),
    ('꾸준한 움직임', '오늘 25분 이상 운동하세요.', 'daily', 'duration', 1500, 160, 320),
    ('불타는 열정', '오늘 30분 운동하기.', 'daily', 'duration', 1800, 240, 480),
    ('운동 매니아', '오늘 40분 운동하기.', 'daily', 'duration', 2400, 400, 800),

    # DAILY - calories
    ('지방 연소 작전 1', '오늘 100kcal 소모하기.', 'daily', 'calories', 100, 80, 160),
    ('지방 연소 작전 2', '오늘 200kcal 소모하기.', 'daily', 'calories', 200, 160, 320),
    ('에너지 폭발', '오늘 300kcal 소모하기.', 'daily', 'calories', 300, 220, 440),
    ('고강도 인터벌', '오늘 400kcal 소모하기.', 'daily', 'calories', 400, 400, 800),
    ('칼로리 버닝', '오늘 500kcal 소모하기.', 'daily', 'calories', 500, 550, 1100),

    # WEEKLY - distance
    ('주간 러닝 스타트', '이번 주 총 10km 달성하기.', 'weekly', 'distance', 10.0, 250, 500),
    ('주간 거리 사냥꾼 1', '이번 주 총 12km 달성하기.', 'weekly', 'distance', 12.0, 450, 900),
    ('주간 거리 사냥꾼 2', '이번 주 총 15km 달성하기.', 'weekly', 'distance', 15.0, 700, 1400),
    ('꾸준한 러너', '이번 주 총 17km 달성하기.', 'weekly', 'distance', 17.0, 1000, 2000),
    ('철인 준비생', '이번 주 총 20km 달성하기.', 'weekly', 'distance', 20.0, 1500, 3000),

    # WEEKLY - duration
    ('일주일의 성실함 1', '이번 주 누적 2시간 운동하기.', 'weekly', 'duration', 7200, 350, 700),
    ('일주일의 성실함 2', '이번 주 누적 3시간 운동하기.', 'weekly', 'duration', 10800, 550, 1100),
    ('운동 루틴 완성', '이번 주 누적 4시간 운동하기.', 'weekly', 'duration', 14400, 900, 1800),

    # WEEKLY - calories
    ('칼로리 파괴자 1', '이번 주 총 700kcal 소모하기.', 'weekly', 'calories', 700, 350, 700),
    ('칼로리 파괴자 2', '이번 주 총 1000kcal 소모하기.', 'weekly', 'calories', 1000, 700, 1400),
    ('지방 불태우기', '이번 주 총 1500kcal 소모하기.', 'weekly', 'calories', 1500, 1300, 2600),

    # MONTHLY - distance
    ('월간 러닝 챌린지 1', '이번 달 총 35km 달리기.', 'monthly', 'distance', 35.0, 1200, 2400),
    ('월간 러닝 챌린지 2', '이번 달 총 40km 달리기.', 'monthly', 'distance', 40.0, 2200, 4400),
    ('마라톤 준비생', '이번 달 총 50km 달리기.', 'monthly', 'distance', 50.0, 5000, 10000),

    # MONTHLY - duration
    ('꾸준함의 상징', '이번 달 누적 15시간 운동하기.', 'monthly', 'duration', 54000, 1500, 3000),
    ('체력왕 도전', '이번 달 누적 20시간 운동하기.', 'monthly', 'duration', 72000, 3200, 6400),
    ('강철의 체력', '이번 달 누적 25시간 운동하기.', 'monthly', 'duration', 90000, 7000, 14000),

    # MONTHLY - calories
    ('월간 지방 연소 1', '이번 달 총 3000kcal 소모하기.', 'monthly', 'calories', 3000, 1800, 3600),
    ('월간 지방 연소 2', '이번 달 총 4000kcal 소모하기.', 'monthly', 'calories', 4000, 4000, 8000),
    ('칼로리 괴물', '이번 달 총 5500kcal 소모하기.', 'monthly', 'calories', 5500, 9000, 18000),
]

active_titles = [title for title, *_ in QUEST_DATA]

Quest.objects.exclude(title__in=active_titles).update(is_active=False)

for title, description, quest_type, metric, target_value, reward_xp, reward_points in QUEST_DATA:
    Quest.objects.update_or_create(
        title=title,
        defaults={
            "description": description,
            "quest_type": quest_type,
            "metric": metric,
            "target_value": target_value,
            "reward_xp": reward_xp,
            "reward_points": reward_points,
            "is_active": True,
        }
    )

daily = Quest.objects.filter(quest_type="daily", is_active=True).count()
weekly = Quest.objects.filter(quest_type="weekly", is_active=True).count()
monthly = Quest.objects.filter(quest_type="monthly", is_active=True).count()
total = Quest.objects.filter(is_active=True).count()

print("daily", daily)
print("weekly", weekly)
print("monthly", monthly)
print("total", total)
