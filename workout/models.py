from django.db import models
from user.models import User

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
