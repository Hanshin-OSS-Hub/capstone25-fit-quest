from rest_framework import serializers
from .models import Exercise, ExerciseLog, RunningSession, Quest, UserQuestProgress

# ==========================================
# 1. 일반 운동 및 스트레칭
# ==========================================
class ExerciseSerializer(serializers.ModelSerializer):
    class Meta:
        model = Exercise
        fields = ['id', 'name', 'category', 'calories_per_minute']

class ExerciseLogSerializer(serializers.ModelSerializer):
    # 프론트엔드에서 보여주기 편하도록 운동 이름도 같이 넘겨줍니다!
    exercise_name = serializers.CharField(source='exercise.name', read_only=True)

    class Meta:
        model = ExerciseLog
        fields = ['id', 'exercise', 'exercise_name', 'duration_minutes', 'calories_burned', 'created_at']
        read_only_fields = ['id', 'calories_burned', 'created_at']


# ==========================================
# 2. 러닝 기록
# ==========================================
class RunningSessionSerializer(serializers.ModelSerializer):
    # 페이스와 칼로리는 서버에서 계산하므로 읽기 전용(read_only)으로 설정
    avg_pace_min_per_km = serializers.DecimalField(max_digits=5, decimal_places=2, read_only=True)
    calories_burned = serializers.IntegerField(read_only=True)

    class Meta:
        model = RunningSession
        fields = [
            "id", "distance_km", "duration_sec", "avg_pace_min_per_km", "current_pace_min_per_km",
            "calories_burned", "start_time", "end_time", "created_at"
        ]
        read_only_fields = ["id", "created_at"]

    def validate(self, data):
        """운동 데이터의 논리적 오류 검증"""
        if data.get('distance_km', 0) <= 0:
            raise serializers.ValidationError("거리는 0km보다 커야 합니다.")
        if data.get('duration_sec', 0) <= 0:
            raise serializers.ValidationError("시간은 0초보다 커야 합니다.")
        if data.get('start_time') and data.get('end_time'):
            if data['start_time'] > data['end_time']:
                raise serializers.ValidationError("종료 시간이 시작 시간보다 빠를 수 없습니다.")
        return data

    def create(self, validated_data):
        """데이터 저장 전 서버에서 통계치 자동 계산"""
        distance = float(validated_data['distance_km'])
        duration_sec = validated_data['duration_sec']

        # 1. 평균 페이스 계산 (분/km)
        if distance > 0:
            duration_min = duration_sec / 60
            validated_data['avg_pace_min_per_km'] = round(duration_min / distance, 2)
        
        # 2. 칼로리 계산 (단순 공식: 70kg 성인 기준 1km당 70kcal)
        validated_data['calories_burned'] = int(distance * 70)

        return super().create(validated_data)


# ==========================================
# 3. 퀘스트 관련
# ==========================================
class QuestSerializer(serializers.ModelSerializer):
    class Meta:
        model = Quest
        fields = "__all__"

class UserQuestProgressSerializer(serializers.ModelSerializer):
    """유저의 퀘스트 진행도: 퀘스트의 상세 정보를 함께 제공"""
    quest_name = serializers.CharField(source="quest.title", read_only=True)
    quest_desc = serializers.CharField(source="quest.description", read_only=True)
    target_value = serializers.FloatField(source="quest.target_value", read_only=True)
    reward_xp = serializers.IntegerField(source="quest.reward_xp", read_only=True)
    reward_points = serializers.IntegerField(source="quest.reward_points", read_only=True)

    class Meta:
        model = UserQuestProgress
        fields = [
            "id", "quest", "quest_name", "quest_desc", "target_value", 
            "progress_value", "is_completed", "reward_xp", "reward_points", 
            "cycle_key", "completed_at"
        ]
        read_only_fields = ["is_completed", "completed_at"]
def process_running_log(running_session):
    from .models import UserQuestProgress
    from datetime import date
    user = running_session.user
    today_key = date.today().strftime('%Y-%m-%d')
    progress_list = UserQuestProgress.objects.filter(user=user, cycle_key=today_key)
    for p in progress_list:
        quest = p.quest
        if p.is_completed: continue
        if quest.metric == 'distance':
            p.progress_value += float(running_session.distance_km)
        elif quest.metric == 'duration':
            p.progress_value += float(running_session.duration_sec)
        elif quest.metric == 'calories':
            p.progress_value += float(running_session.calories_burned or 0)
        if p.progress_value >= quest.target_value:
            p.is_completed = True
        p.save()
