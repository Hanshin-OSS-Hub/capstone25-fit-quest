cd ~/capstone25-fit-quest/BackEnd

cat > update_quests.py <<'EOF'
import os
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")
django.setup()

from workout.models import Quest


QUEST_DATA = [
    # DAILY
    ("가벼운 첫걸음 1", "오늘 1km 가볍게 달려보세요.", "daily", "distance", 1.0, 20, 40),
    ("가벼운 첫걸음 2", "오늘 1.5km 달려보세요.", "daily", "distance", 1.5, 40, 80),
    ("동네 한바퀴", "부담 없이 2km 뛰어보세요.", "daily", "distance", 2.0, 60, 120),
    ("번개 러너", "짧고 굵게 3km 달리기!", "daily", "distance", 3.0, 80, 160),
    ("기초 체력 강화", "오늘 4km 달리기에 도전하세요.", "daily", "distance", 4.0, 120, 240),
    ("중급 러너", "오늘 5km 달리기에 도전하세요.", "daily", "distance", 5.0, 200, 400),
    ("열정 러너", "오늘 7km 장거리 러닝!", "daily", "distance", 7.0, 300, 600),
    ("장거리 마스터", "오늘 10km 달리기 성공하기.", "daily", "distance", 10.0, 450, 900),

    ("30분 챌린지", "오늘 30분 이상 운동하세요.", "daily", "duration", 1800, 100, 200),
    ("꾸준한 움직임", "오늘 45분 이상 운동하세요.", "daily", "duration", 2700, 160, 320),
    ("한 시간의 열정", "오늘 60분 운동하기.", "daily", "duration", 3600, 240, 480),
    ("운동 중독자", "오늘 90분 운동하기.", "daily", "duration", 5400, 400, 800),

    ("지방 연소 작전 1", "오늘 200kcal 소모하기.", "daily", "calories", 200, 80, 160),
    ("지방 연소 작전 2", "오늘 400kcal 소모하기.", "daily", "calories", 400, 160, 320),
    ("에너지 폭발", "오늘 500kcal 소모하기.", "daily", "calories", 500, 220, 440),
    ("고강도 인터벌", "오늘 800kcal 소모하기.", "daily", "calories", 800, 400, 800),
    ("대식가 탈출", "오늘 1000kcal 소모하기.", "daily", "calories", 1000, 550, 1100),

    # WEEKLY
    ("주간 러닝 스타트", "이번 주 총 10km 달성하기.", "weekly", "distance", 10.0, 250, 500),
    ("주간 거리 사냥꾼 1", "이번 주 총 15km 달성하기.", "weekly", "distance", 15.0, 450, 900),
    ("주간 거리 사냥꾼 2", "이번 주 총 20km 달성하기.", "weekly", "distance", 20.0, 700, 1400),
    ("꾸준한 러너", "이번 주 총 25km 달성하기.", "weekly", "distance", 25.0, 1000, 2000),
    ("철인 준비생", "이번 주 총 30km 달성하기.", "weekly", "distance", 30.0, 1500, 3000),

    ("일주일의 성실함 1", "이번 주 누적 8시간 운동하기.", "weekly", "duration", 28800, 350, 700),
    ("일주일의 성실함 2", "이번 주 누적 9시간 운동하기.", "weekly", "duration", 32400, 550, 1100),
    ("운동 루틴 완성", "이번 주 누적 10시간 운동하기.", "weekly", "duration", 36000, 900, 1800),

    ("칼로리 파괴자 1", "이번 주 총 1000kcal 소모하기.", "weekly", "calories", 1000, 350, 700),
    ("칼로리 파괴자 2", "이번 주 총 2000kcal 소모하기.", "weekly", "calories", 2000, 700, 1400),
    ("지방 불태우기", "이번 주 총 3500kcal 소모하기.", "weekly", "calories", 3500, 1300, 2600),

    # MONTHLY
    ("월간 러닝 챌린지 1", "이번 달 총 100km 달리기.", "monthly", "distance", 100.0, 1200, 2400),
    ("월간 러닝 챌린지 2", "이번 달 총 150km 달리기.", "monthly", "distance", 150.0, 2200, 4400),
    ("마라톤 준비생", "이번 달 총 200km 달리기.", "monthly", "distance", 200.0, 5000, 10000),

    ("꾸준함의 상징", "이번 달 누적 30시간 운동하기.", "monthly", "duration", 108000, 1500, 3000),
    ("체력왕 도전", "이번 달 누적 40시간 운동하기.", "monthly", "duration", 144000, 3200, 6400),
    ("철인 28호", "이번 달 누적 50시간 운동하기.", "monthly", "duration", 180000, 7000, 14000),

    ("월간 지방 연소 1", "이번 달 총 5000kcal 소모하기.", "monthly", "calories", 5000, 1800, 3600),
    ("월간 지방 연소 2", "이번 달 총 10000kcal 소모하기.", "monthly", "calories", 10000, 4000, 8000),
    ("칼로리 괴물", "이번 달 총 20000kcal 소모하기.", "monthly", "calories", 20000, 9000, 18000),
]


active_titles = [q[0] for q in QUEST_DATA]

Quest.objects.exclude(title__in=active_titles).update(is_active=False)

created_count = 0
updated_count = 0

for title, description, quest_type, metric, target_value, reward_xp, reward_points in QUEST_DATA:
    obj, created = Quest.objects.update_or_create(
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

    if created:
        created_count += 1
    else:
        updated_count += 1

print("퀘스트 업데이트 완료")
print("생성:", created_count)
print("수정:", updated_count)
print("활성 daily:", Quest.objects.filter(quest_type="daily", is_active=True).count())
print("활성 weekly:", Quest.objects.filter(quest_type="weekly", is_active=True).count())
print("활성 monthly:", Quest.objects.filter(quest_type="monthly", is_active=True).count())
print("활성 전체:", Quest.objects.filter(is_active=True).count())
EOF