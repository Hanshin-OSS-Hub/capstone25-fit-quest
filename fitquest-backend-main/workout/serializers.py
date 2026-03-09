from rest_framework import serializers
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from django.contrib.auth import get_user_model
from .models import RunningSession, Quest, UserQuestProgress

User = get_user_model()

# ==========================================
# 1. 사용자 인증 관련 (Signup, Login, Profile)
# ==========================================

class SignupSerializer(serializers.ModelSerializer):
    """회원가입: 비밀번호 해싱 및 최소 길이 검증"""
    password = serializers.CharField(write_only=True, min_length=8)

    class Meta:
        model = User
        fields = ("email", "nickname", "password")

    def create(self, validated_data):
        return User.objects.create_user(**validated_data)


class UserSerializer(serializers.ModelSerializer):
    """내 정보 응답: 모델의 @property(level)를 포함"""
    level = serializers.ReadOnlyField() 

    class Meta:
        model = User
        fields = ['id', 'email', 'nickname', 'level', 'exp', 'point']


class EmailTokenObtainPairSerializer(TokenObtainPairSerializer):
    """로그인: 이메일을 아이디로 사용하는 JWT 발급"""
    username_field = "email"

    def validate(self, attrs):
        email = attrs.get("email")
        password = attrs.get("password")
        user = User.objects.filter(email=email).first()

        if user is None or not user.check_password(password):
            raise serializers.ValidationError("잘못된 이메일 또는 비밀번호입니다.")

        return super().validate(attrs)


# ==========================================
# 2. 러닝 기록 관련 (자동 계산 로직 포함)
# ==========================================

class RunningSessionSerializer(serializers.ModelSerializer):
    # 페이스와 칼로리는 서버에서 계산하므로 읽기 전용으로 설정
    avg_pace_min_per_km = serializers.DecimalField(max_digits=5, decimal_places=2, read_only=True)
    calories_burned = serializers.IntegerField(read_only=True)

    class Meta:
        model = RunningSession
        fields = [
            "id", "distance_km", "duration_sec", "avg_pace_min_per_km", 
            "calories_burned", "start_time", "end_time", "created_at"
        ]
        read_only_fields = ["id", "created_at"]

    def validate(self, data):
        """운동 데이터의 논리적 오류 검증"""
        if data.get('distance_km', 0) <= 0:
            raise serializers.ValidationError("거리는 0km보다 커야 합니다.")
        if data.get('duration_sec', 0) <= 0:
            raise serializers.ValidationError("시간은 0초보다 커야 합니다.")
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
# 3. 퀘스트 관련 (상세 정보 포함)
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