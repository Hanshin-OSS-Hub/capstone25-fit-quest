from django.db import models
from django.contrib.auth.models import AbstractUser
from django.contrib.auth.base_user import BaseUserManager
from django.conf import settings
from django.utils import timezone

# --------------------------------------------------
# 1. 사용자 관리 (Custom User & Manager)
# --------------------------------------------------


# --------------------------------------------------
# 2. 소셜 로그인 (Social Account)
# --------------------------------------------------

class RunningSession(models.Model):
    # 유저를 참조할 때는 settings.AUTH_USER_MODEL을 사용
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    distance_km = models.DecimalField(max_digits=5, decimal_places=2)
    duration_sec = models.IntegerField()
    avg_pace_min_per_km = models.DecimalField(max_digits=5, decimal_places=2)
    calories_burned = models.IntegerField(default=0)
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.nickname} - {self.distance_km}km"

# --------------------------------------------------
# 3. 러닝 및 퀘스트 (Workout & Quests)
# --------------------------------------------------

class RunningSession(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    distance_km = models.DecimalField(max_digits=5, decimal_places=2)
    duration_sec = models.IntegerField()
    
    # 서버 계산 필드
    avg_pace_min_per_km = models.DecimalField(max_digits=5, decimal_places=2)
    calories_burned = models.IntegerField(default=0)
    
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.nickname} - {self.distance_km}km"

class Quest(models.Model):
    QUEST_TYPES = (('daily', 'Daily'), ('weekly', 'Weekly'), ('monthly', 'Monthly'))
    METRIC_TYPES = (('distance', 'Distance'), ('duration', 'Duration'), ('calories', 'Calories'))

    title = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    quest_type = models.CharField(max_length=10, choices=QUEST_TYPES)
    metric = models.CharField(max_length=10, choices=METRIC_TYPES, default='distance')
    target_value = models.FloatField(default=1)
    reward_xp = models.IntegerField(default=0)
    reward_points = models.IntegerField(default=0)
    is_active = models.BooleanField(default=True)

    def __str__(self):
        return self.title

class UserQuestProgress(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    quest = models.ForeignKey(Quest, on_delete=models.CASCADE)
    progress_value = models.FloatField(default=0)
    is_completed = models.BooleanField(default=False)
    
    # 주기를 구분하는 키 (예: "2026-03-09")
    cycle_key = models.CharField(max_length=20, default="", db_index=True)
    
    completed_at = models.DateTimeField(null=True, blank=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ('user', 'quest', 'cycle_key')

# --------------------------------------------------
# 4. 부가 정보 (Settings & Streak)
# --------------------------------------------------

