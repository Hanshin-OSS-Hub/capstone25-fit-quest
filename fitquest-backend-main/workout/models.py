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

    title = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    quest_type = models.CharField(max_length=10, choices=QUEST_TYPES)
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
    completed_at = models.DateTimeField(null=True, blank=True)

    def update_progress(self, value):
        """ 값 추가 후 자동 체크 """
        if not self.is_completed:
            self.progress_value += value
            if self.progress_value >= self.quest.target_value:
                self.is_completed = True
                self.completed_at = timezone.now()
                self.user.exp += self.quest.reward_xp
                self.user.point += self.quest.reward_points
                self.user.save()
            self.save()


User = get_user_model()

class RunningSession(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    distance_km = models.DecimalField(max_digits=5, decimal_places=2)
    duration_sec = models.IntegerField()
    avg_pace_min_per_km = models.DecimalField(max_digits=4, decimal_places=2)
    current_pace_min_per_km = models.DecimalField(max_digits=4, decimal_places=2, null=True, blank=True)
    calories_burned = models.DecimalField(max_digits=6, decimal_places=2, null=True, blank=True)
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.email} - {self.distance_km}km"


