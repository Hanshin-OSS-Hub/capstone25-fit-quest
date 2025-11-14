from django.db import models
from user.models import User

class Quest(models.Model):
    QUEST_TYPE = [('day', 'Day'), ('week', 'Week'), ('month', 'Month')]
    DIFFICULTY = [('easy', 'Easy'), ('normal', 'Normal'), ('hard', 'Hard')]

    quest_type = models.CharField(max_length=10, choices=QUEST_TYPE)
    difficulty = models.CharField(max_length=10, choices=DIFFICULTY)
    quest_name = models.CharField(max_length=100)
    quest_description = models.CharField(max_length=255)
    exp = models.IntegerField(default=0)
    start_time = models.DateTimeField(null=True, blank=True)
    end_time = models.DateTimeField(null=True, blank=True)
    is_completed = models.BooleanField(default=False)
    streak_days = models.IntegerField(default=0)
