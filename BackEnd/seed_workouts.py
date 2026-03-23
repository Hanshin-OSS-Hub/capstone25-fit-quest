import os
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")
django.setup()

from workout.models import Workout

# 운동 데이터 리스트 
workout_data = [
    # 스트레칭 (Lv 1)
    {"name": "목/어깨 롤링", "category": "stretching", "target_muscle": "상체", "level": 1, "equipment": "맨몸", "duration_or_reps": "1분"},
    {"name": "고양이-소 자세", "category": "stretching", "target_muscle": "코어/등", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회"},
    {"name": "코브라 자세", "category": "stretching", "target_muscle": "복부", "level": 1, "equipment": "맨몸", "duration_or_reps": "30초"},
    {"name": "나비 자세", "category": "stretching", "target_muscle": "하체/골반", "level": 1, "equipment": "맨몸", "duration_or_reps": "1분"},
    {"name": "햄스트링 늘리기", "category": "stretching", "target_muscle": "하체", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 30초"},
    {"name": "전신 기지개 (아동 자세)", "category": "stretching", "target_muscle": "전신", "level": 1, "equipment": "맨몸", "duration_or_reps": "1분"},
    
    
    # 근력 - 레벨 1
    {"name": "인클라인 푸시업", "category": "strength", "target_muscle": "가슴/어깨", "level": 1, "equipment": "벽/책상", "duration_or_reps": "10회 3세트"},
    {"name": "니 플랭크 (무릎 대고)", "category": "strength", "target_muscle": "코어", "level": 1, "equipment": "맨몸", "duration_or_reps": "20초 3세트"},
    {"name": "하프 스쿼트", "category": "strength", "target_muscle": "하체", "level": 1, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    {"name": "브릿지 (엉덩이 들기)", "category": "strength", "target_muscle": "하체/허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    {"name": "버드독", "category": "strength", "target_muscle": "코어/전신", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 10회 3세트"},
    {"name": "사이드 레그 레이즈", "category": "strength", "target_muscle": "골반/하체", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15회 3세트"},
    
    # 근력 - 레벨 2
    {"name": "니 푸시업 (무릎 대고)", "category": "strength", "target_muscle": "가슴/어깨", "level": 2, "equipment": "맨몸", "duration_or_reps": "10회 3세트"},
    {"name": "스탠다드 스쿼트", "category": "strength", "target_muscle": "하체", "level": 2, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    {"name": "스탠다드 플랭크", "category": "strength", "target_muscle": "코어", "level": 2, "equipment": "맨몸", "duration_or_reps": "30초 3세트"},
    {"name": "크런치 (상복부)", "category": "strength", "target_muscle": "코어", "level": 2, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    {"name": "제자리 런지", "category": "strength", "target_muscle": "하체", "level": 2, "equipment": "맨몸", "duration_or_reps": "양쪽 10회 3세트"},
    {"name": "슈퍼맨 맨몸 로우", "category": "strength", "target_muscle": "등/후면", "level": 2, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    
    # 근력 - 레벨 3
    {"name": "스탠다드 푸시업", "category": "strength", "target_muscle": "가슴/어깨", "level": 3, "equipment": "맨몸", "duration_or_reps": "10회 3세트"},
    {"name": "와이드 스쿼트", "category": "strength", "target_muscle": "하체 안쪽", "level": 3, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    {"name": "사이드 플랭크", "category": "strength", "target_muscle": "코어 측면", "level": 3, "equipment": "맨몸", "duration_or_reps": "양쪽 20초 3세트"},
    {"name": "체어 딥스", "category": "strength", "target_muscle": "삼두/어깨", "level": 3, "equipment": "의자", "duration_or_reps": "10회 3세트"},
    {"name": "워킹 런지", "category": "strength", "target_muscle": "하체 전반", "level": 3, "equipment": "맨몸", "duration_or_reps": "양쪽 12회 3세트"},
    {"name": "레그 레이즈 (하복부)", "category": "strength", "target_muscle": "코어", "level": 3, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    
    # 근력 - 레벨 4
    {"name": "파이크 푸시업", "category": "strength", "target_muscle": "어깨 집중", "level": 4, "equipment": "맨몸", "duration_or_reps": "10회 3세트"},
    {"name": "불가리안 스플릿 스쿼트", "category": "strength", "target_muscle": "엉덩이/하체", "level": 4, "equipment": "의자", "duration_or_reps": "양쪽 10회 3세트"},
    {"name": "덤벨 로우", "category": "strength", "target_muscle": "등", "level": 4, "equipment": "덤벨", "duration_or_reps": "12회 3세트"},
    
    # 근력 - 레벨 5
    {"name": "디클라인 푸시업", "category": "strength", "target_muscle": "가슴 상부", "level": 5, "equipment": "의자", "duration_or_reps": "10회 3세트"},
    {"name": "점프 스쿼트", "category": "strength", "target_muscle": "하체/파워", "level": 5, "equipment": "맨몸", "duration_or_reps": "15회 3세트"},
    {"name": "할로우 홀드", "category": "strength", "target_muscle": "코어 끝판왕", "level": 5, "equipment": "맨몸", "duration_or_reps": "30초 3세트"}
]

#  DB에 집어넣기
print("운동 데이터 삽입을 시작합니다")
count = 0
for data in workout_data:
    obj, created = Workout.objects.get_or_create(**data)
    if created:
        count += 1

print(f" 총 {count}개의 운동 데이터가 성공적으로 DB에 저장되었습니다")