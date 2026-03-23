from rest_framework import serializers
from datetime import date
from .models import (
    Workout, Quest, Achievement, UserAchievement, WorkoutLog, RunningSession, UserQuestProgress
)

# 러닝 기록 시 퀘스트 진행도 업데이트 함수
def process_running_log(running_session):
    user = running_session.user
    today_key = date.today().strftime('%Y-%m-%d')
    progress_list = UserQuestProgress.objects.filter(user=user, cycle_key=today_key)
    
    for p in progress_list:
        quest = p.quest
        if p.is_completed: 
            continue
            
        if quest.metric == 'distance':
            p.progress_value += float(running_session.distance_km)
        elif quest.metric == 'duration':
            p.progress_value += float(running_session.duration_sec)
        elif quest.metric == 'calories':
            p.progress_value += float(running_session.calories_burned or 0)
            
        if p.progress_value >= quest.target_value:
            p.is_completed = True
            p.completed_at = date.today() # 팀원 필드 반영
        p.save()


# ==========================================
# 1. 일반 운동 및 스트레칭
# ==========================================
class ExerciseSerializer(serializers.ModelSerializer):
    class Meta:
        model = Workout
        fields = ['id', 'name', 'category', 'calories_per_minute']

class ExerciseLogSerializer(serializers.ModelSerializer):
    exercise_name = serializers.CharField(source='workout.name', read_only=True)

    class Meta:
        model = WorkoutLog
        fields = ['id', 'workout', 'exercise_name', 'duration_minutes', 'calories_burned', 'created_at']
        read_only_fields = ['id', 'calories_burned', 'created_at']


# ==========================================
# 2. 러닝 기록
# ==========================================
class RunningSessionSerializer(serializers.ModelSerializer):
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
        if data.get('distance_km', 0) <= 0:
            raise serializers.ValidationError("거리는 0km보다 커야 합니다.")
        if data.get('duration_sec', 0) <= 0:
            raise serializers.ValidationError("시간은 0초보다 커야 합니다.")
        if data.get('start_time') and data.get('end_time'):
            if data['start_time'] > data['end_time']:
                raise serializers.ValidationError("종료 시간이 시작 시간보다 빠를 수 없습니다.")
        return data

    def create(self, validated_data):
        distance = float(validated_data['distance_km'])
        duration_sec = validated_data['duration_sec']

        if distance > 0:
            duration_min = duration_sec / 60
            validated_data['avg_pace_min_per_km'] = round(duration_min / distance, 2)
        
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


# ==========================================
# 4. 홈 트레이닝 도감 
# ==========================================
class WorkoutSerializer(serializers.ModelSerializer):
    # '스트레칭'처럼 한글로 이름을 보여주는 기능
    category_display = serializers.CharField(source='get_category_display', read_only=True)

    class Meta:
        model = Workout
        fields = ['id', 'name', 'category', 'category_display', 'target_muscle', 'level', 'equipment', 'duration_or_reps']


# ==========================================
# 5. 업적 및 칭호 
# ==========================================
class AchievementSerializer(serializers.ModelSerializer):
    is_achieved = serializers.SerializerMethodField()
    target_value = serializers.FloatField() 

    class Meta:
        model = Achievement
        fields = [
            'id', 'name', 'description', 'metric', 
            'target_value', 'reward_title', 'is_achieved'
        ]

    def get_is_achieved(self, obj):
        request = self.context.get('request')
        if request and request.user.is_authenticated:
            return UserAchievement.objects.filter(
                user=request.user, 
                achievement=obj
            ).exists()
        return False