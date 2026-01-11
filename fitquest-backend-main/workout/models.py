from django.db import models
from django.contrib.auth import get_user_model
from django.db import models
from django.utils import timezone
from fitquest.models import CustomUser


class Quest(models.Model):
    QUEST_TYPES = (
        ('daily', 'Daily'),
        ('weekly', 'Weekly'),
        ('monthly', 'Monthly'),
    )

    METRIC_TYPES = (
        ('distance', 'Distance (km)'),
        ('duration', 'Duration (min)'),
        ('calories', 'Calories (kcal)'),
    )

    title = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    quest_type = models.CharField(max_length=10, choices=QUEST_TYPES)
    metric = models.CharField(max_length=10, choices=METRIC_TYPES, default='distance')
    target_value = models.FloatField(default=1)
    reward_xp = models.IntegerField(default=0)
    reward_points = models.IntegerField(default=0)
    is_active = models.BooleanField(default=True)

    def __str__(self):
        return f"{self.title} ({self.quest_type})"


class UserQuestProgress(models.Model):
    user = models.ForeignKey(CustomUser, on_delete=models.CASCADE)
    quest = models.ForeignKey(Quest, on_delete=models.CASCADE)
    
    progress_value = models.FloatField(default=0)
    is_completed = models.BooleanField(default=False)
    
    # [핵심 변경] 언제 완료한 퀘스트인지 '주기'를 기록 (예: "2024-05-20" 또는 "2024-W21")
    cycle_key = models.CharField(max_length=20, default="", db_index=True)
    
    completed_at = models.DateTimeField(null=True, blank=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        # 유저+퀘스트+주기 3가지가 합쳐서 유니크해야 함 (중복 생성 방지)
        unique_together = ('user', 'quest', 'cycle_key')


User = get_user_model()

class RunningSession(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    distance_km = models.DecimalField(max_digits=5, decimal_places=2)
    duration_sec = models.IntegerField()
    
    # 서버가 계산해서 넣을 필드들
    avg_pace_min_per_km = models.DecimalField(max_digits=5, decimal_places=2)
    calories_burned = models.IntegerField(default=0)  # ★ 필드 추가!
    
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.email} - {self.distance_km}km"



