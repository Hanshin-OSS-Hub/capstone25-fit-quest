# fitquest/serializers.py
from django.contrib.auth import get_user_model
from rest_framework import serializers
from workout.models import Workout, Quest, Achievement, UserAchievement 
from workout.models import RunningSession, ExerciseLog
from datetime import date, timedelta

from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

User = get_user_model()

# --- 회원가입 ---
class SignupSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)

    class Meta:
        model = User
        fields = ("email", "nickname", "password")

    def create(self, validated_data):
        password = validated_data.pop("password")
        user = User(**validated_data)
        user.set_password(password)
        user.save()
        return user

# --- 내 정보 응답 ---
class UserSerializer(serializers.ModelSerializer):
    level = serializers.ReadOnlyField()
    monster_tier = serializers.ReadOnlyField()
    attendance_days = serializers.SerializerMethodField()
    streak_days = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ['id', 'email', 'nickname', 'level', 'exp', 'monster_tier', 'current_title','attendance_days', 'streak_days']
        read_only_fields = ['level', 'monster_tier','attendance_days', 'streak_days']
    
    
    def _get_activity_dates(self, obj):
        running_dates = set(
            RunningSession.objects.filter(user=obj)
            .values_list("start_time__date", flat=True)
        )

        exercise_dates = set(
            ExerciseLog.objects.filter(user=obj)
            .values_list("created_at__date", flat=True)
        )

        all_dates = running_dates | exercise_dates
        return sorted(all_dates, reverse=True)
    

    def get_attendance_days(self, obj):
        return len(self._get_activity_dates(obj))
    

    def get_streak_days(self, obj):
        dates = self._get_activity_dates(obj)

        if not dates:
            return 0

        today = date.today()

        if dates[0] == today:
            expected = today
        elif dates[0] == today - timedelta(days=1):
            expected = today - timedelta(days=1)
        else:
            return 0

        streak = 0
        for d in dates:
            if d == expected:
                streak += 1
                expected = expected - timedelta(days=1)
            elif d < expected:
                break

        return streak
    
class EmailTokenObtainPairSerializer(TokenObtainPairSerializer):
    username_field = "email"

    def validate(self, attrs):
        email = attrs.get("email")
        password = attrs.get("password")

        user = User.objects.filter(email=email).first()
        if user is None:
            raise serializers.ValidationError("잘못된 이메일 또는 비밀번호입니다.")

        if not user.check_password(password):
            raise serializers.ValidationError("잘못된 이메일 또는 비밀번호입니다.")

        # 정상 로그인 → 부모 validate 호출해 JWT 발급
        data = super().validate(attrs)
        return data
    
# --- JWT 발급 유틸 ---
def issue_tokens_for_user(user: User) -> dict:
    refresh = RefreshToken.for_user(user)
    return {"access": str(refresh.access_token), "refresh": str(refresh)}

class AchievementSerializer(serializers.ModelSerializer):
    class Meta:
        model = Achievement
        fields = '__all__'
        ref_name = 'FitquestAchievement'


#랭킹 전용 (이메일 제거)
class RankingUserSerializer(serializers.ModelSerializer):
    level = serializers.ReadOnlyField()
    monster_tier = serializers.ReadOnlyField()

    class Meta:
        model = User
        fields = ["id", "nickname", "level", "exp", "current_title", "monster_tier"]
        read_only_fields = fields