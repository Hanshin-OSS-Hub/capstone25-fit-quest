from rest_framework import serializers
from .models import RunningSession, Quest, UserQuestProgress

class QuestSerializer(serializers.ModelSerializer):
    class Meta:
        model = Quest
        fields = [
            "id",
            "title",
            "description",
            "quest_type",
            "target_value",
            "reward_xp",
            "reward_points",
            "is_active",
        ]


class UserQuestProgressSerializer(serializers.ModelSerializer):
    quest_name = serializers.CharField(source="quest.title", read_only=True)
    quest_desc = serializers.CharField(source="quest.description", read_only=True)
    target_value = serializers.FloatField(source="quest.target_value", read_only=True)
    reward_xp = serializers.IntegerField(source="quest.reward_xp", read_only=True)
    reward_points = serializers.IntegerField(source="quest.reward_points", read_only=True)

    class Meta:
        model = UserQuestProgress
        fields = [
            "id",
            "quest",
            "quest_name",
            "quest_desc",
            "target_value",     # 목표치 (예: 3km)
            "progress_value",   # 현재 진행 (예: 1.5km)
            "is_completed",
            "reward_xp",
            "reward_points",
            "cycle_key",        # 디버깅용 (주기 확인)
            "completed_at",
        ]


class RunningSessionSerializer(serializers.ModelSerializer):
    # 페이스는 서버가 계산하므로 read_only로 설정 (앱에서 보내도 무시함)
    avg_pace_min_per_km = serializers.DecimalField(max_digits=4, decimal_places=2, read_only=True)
    calories_burned = serializers.DecimalField(max_digits=6, decimal_places=2, read_only=True)

    class Meta:
        model = RunningSession
        fields = [
            "id",
            "distance_km",
            "duration_sec",
            "avg_pace_min_per_km",  # 서버 계산
            "current_pace_min_per_km",
            "calories_burned",      # 서버 계산 (단순 공식)
            "start_time",
            "end_time",
            "created_at",
        ]
        read_only_fields = ["id", "created_at"]

    def validate(self, data):
        """데이터 유효성 검사 (Guardrails)"""
        # 1. 거리와 시간 검증
        if data.get('distance_km', 0) <= 0:
            raise serializers.ValidationError("거리는 0km보다 커야 합니다.")
        
        if data.get('duration_sec', 0) <= 0:
            raise serializers.ValidationError("운동 시간은 0초보다 커야 합니다.")

        # 2. 시간 역전 검증
        start = data.get('start_time')
        end = data.get('end_time')
        if start and end and start > end:
            raise serializers.ValidationError("종료 시간이 시작 시간보다 빠를 수 없습니다.")

        return data

    def create(self, validated_data):
        """저장 전 서버에서 통계 자동 계산"""
        distance = float(validated_data['distance_km'])
        duration_sec = validated_data['duration_sec']

        # 1. 평균 페이스 계산
        if distance > 0:
            duration_min = duration_sec / 60
            calculated_pace = duration_min / distance
            validated_data['avg_pace_min_per_km'] = round(calculated_pace, 2)
        else:
            validated_data['avg_pace_min_per_km'] = 0

        # 2. 칼로리 계산 (표준 공식: METs * 체중 * 시간)
        # 간단 공식: 체중(kg) * 거리(km) (체중 70kg 가정 시)
        # 좀 더 정교한 METs 공식을 쓰고 싶다면 아래처럼 구현
        # (러닝 METs는 대략 8.0 ~ 11.0 사이, 여기선 평균 1km당 70kcal 소모로 단순화 or 공식 적용)
        
        # [단순공식] 1km당 약 60~70kcal 소모 , 나중에 수정
        estimated_calories = distance * 70 
        
        validated_data['calories_burned'] = int(estimated_calories)

        return super().create(validated_data)
    