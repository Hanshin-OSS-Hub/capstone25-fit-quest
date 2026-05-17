import os
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")
django.setup()

from workout.models import Workout


IMAGE_NAME_MAP = {
    "고양이-소 자세 (허리)": "고양이소자세.png",
    "아동 자세 (허리)": "아동자세스트레칭.png",
    "척추 트위스트": "척추트위스트.png",
    "코브라 스트레칭": "코브라스트레칭.png",
    "무릎 당기기": "무릎당기기.png",
    "벽 가슴 스트레칭": "벽가슴스트레칭.png",
    "팔 뒤로 가슴 열기": "팔 뒤로 가슴열기.png",
    "상체 전굴 스트레칭": "상체전굴스트레칭.png",
    "스레드 더 니들": "스레드더니들.png",
    "사이드 스트레칭 (광배)": "사이드스트레칭(광배).png",
    "햄스트링 스트레칭": "햄스트링스트레칭.png",
    "쿼드 스트레칭": "쿼드스트레칭.png",
    "종아리 스트레칭": "종아리스트레칭.png",
    "런지 스트레칭": "런지스트레칭.png",
    "사이드 런지 스트레칭": "사이드런지스트레칭.png",
    "덤벨 사이드 레터럴 레이즈": "덤벨사이드 레터럴레이즈.png",
    "보조 풀업 (밴드/점프)": "보조 풀업(밴드, 점프).png",
    "원암 푸시업 (보조 포함)": "원암 푸쉬업 (보조 포함).png",
}

# 전체 운동 데이터
grouped_workout_data = {
    
    #  1. 스트레칭 (Level 1, 분당 2kcal)
    "stretching": {
        "neck": [
            {"name": "목 측면 스트레칭", "target_muscle": "목", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "목 회전 스트레칭", "target_muscle": "목", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "턱 당기기 (chin tuck)", "target_muscle": "목", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "목 뒤로 젖히기", "target_muscle": "목", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "상부 승모 스트레칭", "target_muscle": "목/승모", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "목 기울이기 스트레칭", "target_muscle": "목", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2}
        ],
        "shoulder": [
            {"name": "암 서클", "target_muscle": "어깨", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "크로스 바디 스트레칭", "target_muscle": "어깨", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "팔 뒤로 당기기", "target_muscle": "어깨", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "벽 밀기 어깨 스트레칭", "target_muscle": "어깨", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "오버헤드 스트레칭", "target_muscle": "어깨", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "어깨 회전 스트레칭", "target_muscle": "어깨", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2}
        ],
        "arm": [
            {"name": "삼두 오버헤드 스트레칭", "target_muscle": "팔", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "이두 스트레칭 (벽)", "target_muscle": "팔", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "손목 굴곡 스트레칭", "target_muscle": "팔", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "손목 신전 스트레칭", "target_muscle": "팔", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "팔 스윙", "target_muscle": "팔", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "삼두 뒤로 늘리기", "target_muscle": "팔", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2}
        ],
        "chest": [
            {"name": "문틀 가슴 스트레칭", "target_muscle": "가슴", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "팔 뒤로 가슴 열기", "target_muscle": "가슴", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "벽 가슴 스트레칭", "target_muscle": "가슴", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "가슴 확장 스트레칭", "target_muscle": "가슴", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "다이나믹 체스트 오프너", "target_muscle": "가슴", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "인클라인 가슴 스트레칭", "target_muscle": "가슴", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2}
        ],
        "waist": [
            {"name": "고양이소자세", "target_muscle": "허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2},
            {"name": "아동자세스트레칭", "target_muscle": "허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "20초", "calories_per_minute": 2},
            {"name": "척추 트위스트", "target_muscle": "허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "코브라 스트레칭", "target_muscle": "허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "무릎 당기기", "target_muscle": "허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "골반 틸트", "target_muscle": "허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2}
        ],
        "back": [
            {"name": "광배근 스트레칭 (오버헤드)", "target_muscle": "등", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "상체 전굴 스트레칭", "target_muscle": "등/허리", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "스레드 더 니들", "target_muscle": "등/견갑", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 10회", "calories_per_minute": 2},
            {"name": "사이드 스트레칭 (광배)", "target_muscle": "등", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2}
        ],
        "leg": [
            {"name": "햄스트링 스트레칭", "target_muscle": "다리", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "쿼드 스트레칭", "target_muscle": "다리", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "나비 자세", "target_muscle": "다리", "level": 1, "equipment": "맨몸", "duration_or_reps": "20초", "calories_per_minute": 2},
            {"name": "종아리 스트레칭", "target_muscle": "다리", "level": 1, "equipment": "맨몸", "duration_or_reps": "15초", "calories_per_minute": 2},
            {"name": "런지 스트레칭", "target_muscle": "다리", "level": 1, "equipment": "맨몸", "duration_or_reps": "양쪽 15초", "calories_per_minute": 2},
            {"name": "사이드 런지 스트레칭", "target_muscle": "다리", "level": 1, "equipment": "맨몸", "duration_or_reps": "10회", "calories_per_minute": 2}
        ]
    },

    # 2. 근력 운동 (Level 1 ~ 5)
    "strength": {
        "level_1": [
           
            {"name": "니 플랭크", "target_muscle": "복근", "level": 1, "equipment": "맨몸", "duration_or_reps": "20초 3세트", "calories_per_minute": 4},
            {"name": "슈퍼맨 홀드", "target_muscle": "등", "level": 1, "equipment": "맨몸", "duration_or_reps": "20초 3세트", "calories_per_minute": 4},
            {"name": "덤벨 컬", "target_muscle": "이두", "level": 1, "equipment": "덤벨", "duration_or_reps": "7회 3세트", "calories_per_minute": 4},
            {"name": "덤벨 숄더 프레스", "target_muscle": "어깨", "level": 1, "equipment": "덤벨", "duration_or_reps": "7회 3세트", "calories_per_minute": 4},
            {"name": "덤벨 사이드 레터럴 레이즈", "target_muscle": "어깨", "level": 1, "equipment": "덤벨", "duration_or_reps": "7회 3세트", "calories_per_minute": 4},
            {"name": "덤벨 슈러그", "target_muscle": "승모", "level": 1, "equipment": "덤벨", "duration_or_reps": "7회 3세트", "calories_per_minute": 4},
            {"name": "덤벨 트라이셉 킥백", "target_muscle": "삼두", "level": 1, "equipment": "덤벨", "duration_or_reps": "7회 3세트", "calories_per_minute": 4},
            {"name": "덤벨 카프 레이즈", "target_muscle": "종아리", "level": 1, "equipment": "덤벨", "duration_or_reps": "7회 3세트", "calories_per_minute": 4}
        ],
        "level_2": [
            {"name": "니 푸시업", "target_muscle": "가슴", "level": 2, "equipment": "맨몸", "duration_or_reps": "8회 3세트", "calories_per_minute": 5},
            {"name": "스탠다드 스쿼트", "target_muscle": "다리", "level": 2, "equipment": "맨몸", "duration_or_reps": "8회 3세트", "calories_per_minute": 5},
            {"name": "리버스 런지", "target_muscle": "다리", "level": 2, "equipment": "맨몸", "duration_or_reps": "양쪽 8회 3세트", "calories_per_minute": 5},
            {"name": "플랭크", "target_muscle": "복근", "level": 2, "equipment": "맨몸", "duration_or_reps": "30초 3세트", "calories_per_minute": 5},
            {"name": "슈퍼맨 풀", "target_muscle": "등", "level": 2, "equipment": "맨몸", "duration_or_reps": "8회 3세트", "calories_per_minute": 5},
            {"name": "벤치 딥스", "target_muscle": "삼두", "level": 2, "equipment": "의자", "duration_or_reps": "8회 3세트", "calories_per_minute": 5},
            {"name": "덤벨 벤치 프레스", "target_muscle": "가슴", "level": 2, "equipment": "덤벨", "duration_or_reps": "8회 3세트", "calories_per_minute": 5},
            {"name": "덤벨 벤트오버 로우", "target_muscle": "등", "level": 2, "equipment": "덤벨", "duration_or_reps": "8회 3세트", "calories_per_minute": 5}
        ],
        "level_3": [
            {"name": "스탠다드 푸시업", "target_muscle": "가슴", "level": 3, "equipment": "맨몸", "duration_or_reps": "10~12회 3세트", "calories_per_minute": 6},
            {"name": "워킹 런지", "target_muscle": "다리", "level": 3, "equipment": "맨몸", "duration_or_reps": "양쪽 10회 3세트", "calories_per_minute": 6},
            {"name": "사이드 플랭크", "target_muscle": "복근", "level": 3, "equipment": "맨몸", "duration_or_reps": "양쪽 20초 3세트", "calories_per_minute": 6},
            {"name": "보조 풀업(밴드/점프)", "target_muscle": "등", "level": 3, "equipment": "맨몸", "duration_or_reps": "5회 3세트", "calories_per_minute": 6},
            {"name": "체어 딥스", "target_muscle": "삼두", "level": 3, "equipment": "의자", "duration_or_reps": "10회 3세트", "calories_per_minute": 6},
            {"name": "덤벨 해머 컬", "target_muscle": "이두", "level": 3, "equipment": "덤벨", "duration_or_reps": "10회 3세트", "calories_per_minute": 6}
        ],
        "level_4": [
            {"name": "디클라인 푸시업", "target_muscle": "가슴 상부", "level": 4, "equipment": "맨몸", "duration_or_reps": "10회 3세트", "calories_per_minute": 7},
            {"name": "불가리안 스플릿 스쿼트", "target_muscle": "다리", "level": 4, "equipment": "맨몸", "duration_or_reps": "양쪽 10회 3세트", "calories_per_minute": 7},
            {"name": "사이드 플랭크 레그 레이즈", "target_muscle": "복근/코어", "level": 4, "equipment": "맨몸", "duration_or_reps": "양쪽 10회 3세트", "calories_per_minute": 7},
            {"name": "풀업 (정자세)", "target_muscle": "등", "level": 4, "equipment": "맨몸", "duration_or_reps": "6~10회 3세트", "calories_per_minute": 7},
            {"name": "다이아몬드 푸시업", "target_muscle": "삼두", "level": 4, "equipment": "맨몸", "duration_or_reps": "10회 3세트", "calories_per_minute": 7},
            {"name": "행잉 레그 레이즈", "target_muscle": "복근", "level": 4, "equipment": "맨몸", "duration_or_reps": "10회 3세트", "calories_per_minute": 7}
        ],
        "level_5": [
            {"name": "원암 푸시업 (보조 포함)", "target_muscle": "가슴", "level": 5, "equipment": "맨몸", "duration_or_reps": "5회 3세트", "calories_per_minute": 9},
            {"name": "피스톨 스쿼트", "target_muscle": "다리", "level": 5, "equipment": "맨몸", "duration_or_reps": "양쪽 6~8회 3세트", "calories_per_minute": 9},
            {"name": "클랩 푸시업", "target_muscle": "가슴/파워", "level": 5, "equipment": "맨몸", "duration_or_reps": "6~10회 3세트", "calories_per_minute": 9},
            {"name": "체스트 투 바 풀업", "target_muscle": "등", "level": 5, "equipment": "맨몸", "duration_or_reps": "6~10회 3세트", "calories_per_minute": 9},
            {"name": "드래곤 플래그", "target_muscle": "코어", "level": 5, "equipment": "맨몸", "duration_or_reps": "5~8회 3세트", "calories_per_minute": 9},
            {"name": "헤비 덤벨 로우", "target_muscle": "등", "level": 5, "equipment": "덤벨", "duration_or_reps": "8~10회 3세트", "calories_per_minute": 9}
        ]
    }
}

# DB에 집어넣기 로직 (자동으로 Flatten 처리)
print("운동 데이터 삽입을 시작합니다...")
count = 0

# 상위 카테고리 (stretching, strength) 순회
for category_name, sub_groups in grouped_workout_data.items():
    
    # 하위 그룹 (neck, shoulder, level_1 등) 순회
    for group_name, workout_list in sub_groups.items():
        
        # 실제 개별 운동 데이터 순회
        for data in workout_list:
            
            data["category"] = category_name
            data["image_name"] = IMAGE_NAME_MAP.get(data["name"], f"{data['name']}.png")
            # DB 삽입 또는 업데이트
            obj, created = Workout.objects.get_or_create(
                name=data["name"],
                defaults=data
            )
            if created:
                count += 1
            else:
                for key, value in data.items():
                    setattr(obj, key, value)
                obj.save()

print(f" 총 {count}개의 새로운 운동 데이터가 성공적으로 DB에 저장되었습니다!")

