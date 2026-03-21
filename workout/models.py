from django.db import models
from django.contrib.auth import get_user_model
from django.utils import timezone

User = get_user_model()

# 1. 운동 종류 모델
class Exercise(models.Model):
    name = models.CharField(max_length=50)
    calories_per_minute = models.FloatField()

    def __str__(self):
        return self.name

# 2. 퀘스트 모델
class Quest(models.Model):
    title = models.CharField(max_length=100)
    description = models.TextField()
    quest_type = models.CharField(max_length=50)
    metric = models.CharField(max_length=50, default='distance')
    target_value = models.FloatField()
    reward_xp = models.IntegerField(default=100)
    is_active = models.BooleanField(default=True)

    def __str__(self):
        return self.title

# 3. 유저별 퀘스트 진행도
class UserQuestProgress(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    quest = models.ForeignKey(Quest, on_delete=models.CASCADE)
    progress_value = models.FloatField(default=0.0)
    is_completed = models.BooleanField(default=False)
    cycle_key = models.CharField(max_length=20) 
    updated_at = models.DateTimeField(auto_now=True)

# 4. 러닝 세션 (GPS 기반)
class RunningSession(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    distance_km = models.FloatField()
    duration_sec = models.PositiveIntegerField()
    calories_burned = models.PositiveIntegerField(null=True, blank=True)
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

# 5. 일반 운동 기록 (기본값 설정으로 에러 해결)
class ExerciseLog(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    exercise = models.ForeignKey(Exercise, on_delete=models.CASCADE)
    duration_minutes = models.PositiveIntegerField()
    calories_burned = models.FloatField()
    # default=timezone.now를 주어 기존 데이터 에러를 방지합니다.
    date = models.DateField(default=timezone.now) 

# 6. 영구 업적 정의
class Achievement(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField()
    reward_title = models.CharField(max_length=50, blank=True, null=True)
    reward_exp = models.IntegerField(default=0)
    metric = models.CharField(max_length=50) 
    target_value = models.FloatField()

    def __str__(self):
        return self.name

# 7. 유저별 업적 달성 현황
class UserAchievement(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="achievements")
    achievement = models.ForeignKey(Achievement, on_delete=models.CASCADE)
    achieved_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('user', 'achievement')

    def __str__(self):
        return f"{self.user.nickname} - {self.achievement.name}"
