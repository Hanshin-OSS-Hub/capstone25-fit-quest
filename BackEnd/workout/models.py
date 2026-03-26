from django.db import models
from django.conf import settings
from django.utils import timezone

# 1. 운동 종류 모델 
class Exercise(models.Model):
    name = models.CharField(max_length=100)
    category = models.CharField(max_length=50, default="일반") # 팀원의 카테고리 필드 추가
    calories_per_minute = models.FloatField()

    def __str__(self):
        return self.name

# 2. 퀘스트 모델 
class Quest(models.Model):
    QUEST_TYPES = (('daily', 'Daily'), ('weekly', 'Weekly'), ('monthly', 'Monthly'))
    METRIC_TYPES = (('distance', 'Distance'), ('duration', 'Duration'), ('calories', 'Calories'))

    title = models.CharField(max_length=100)
    description = models.TextField()
    quest_type = models.CharField(max_length=50, choices=QUEST_TYPES) # 선택지 형식 적용
    metric = models.CharField(max_length=50, choices=METRIC_TYPES, default='distance')
    target_value = models.FloatField()
    reward_xp = models.IntegerField(default=100)
    reward_points = models.IntegerField(default=0) # 팀원의 포인트 보상 추가
    is_active = models.BooleanField(default=True)

    def __str__(self):
        return self.title

# 3. 유저별 퀘스트 진행도 
class UserQuestProgress(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    quest = models.ForeignKey(Quest, on_delete=models.CASCADE)
    progress_value = models.FloatField(default=0.0)
    is_completed = models.BooleanField(default=False)
    cycle_key = models.CharField(max_length=20) 
    completed_at = models.DateTimeField(null=True, blank=True) # 팀원의 완료 일시 추가
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ('user', 'quest', 'cycle_key')

# 4. 러닝 세션 
class RunningSession(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    distance_km = models.DecimalField(max_digits=5, decimal_places=2) # 팀원의 정밀 소수점 적용
    duration_sec = models.PositiveIntegerField()
    # 페이스 계산 필드들 추가
    avg_pace_min_per_km = models.DecimalField(max_digits=5, decimal_places=2, default=0.00)
    current_pace_min_per_km = models.DecimalField(max_digits=5, decimal_places=2, null=True, blank=True)
    calories_burned = models.PositiveIntegerField(null=True, blank=True)
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

# 5. 일반 운동 기록 
class ExerciseLog(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    exercise = models.ForeignKey(Exercise, on_delete=models.CASCADE)
    duration_minutes = models.PositiveIntegerField()
    calories_burned = models.FloatField()
    date = models.DateField(default=timezone.now)
    created_at = models.DateTimeField(auto_now_add=True)

    def save(self, *args, **kwargs):
        # 저장 시 칼로리 자동 계산 
        if not self.calories_burned and self.duration_minutes:
            self.calories_burned = self.exercise.calories_per_minute * self.duration_minutes
        super().save(*args, **kwargs)

# 6. 홈 트레이닝 도감 및 기록 
class Workout(models.Model):
    CATEGORY_CHOICES = (('stretching', '스트레칭'), ('cardio', '유산소'), ('strength', '근력'))
    name = models.CharField(max_length=100)
    category = models.CharField(max_length=20, choices=CATEGORY_CHOICES)
    target_muscle = models.CharField(max_length=50)
    level = models.IntegerField(default=1)
    equipment = models.CharField(max_length=50, default='맨몸')
    duration_or_reps = models.CharField(max_length=50)
    
    def __str__(self):
        return self.name

class WorkoutLog(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    workout = models.ForeignKey(Workout, on_delete=models.CASCADE)
    completed_at = models.DateTimeField(auto_now_add=True)

# 7. 영구 업적 시스템 
class Achievement(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField()
    reward_title = models.CharField(max_length=50, blank=True, null=True)
    reward_exp = models.IntegerField(default=0)
    metric = models.CharField(max_length=50) 
    target_value = models.FloatField()

    def __str__(self):
        return self.name

class UserAchievement(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="achievements")
    achievement = models.ForeignKey(Achievement, on_delete=models.CASCADE)
    achieved_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('user', 'achievement')

    def __str__(self):
        return f"{self.user.nickname} - {self.achievement.name}"