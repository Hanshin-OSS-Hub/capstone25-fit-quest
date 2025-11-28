from django.db import models
from django.conf import settings


class RunningSession(models.Model):
    """
    러닝 기록 모델 (네 담당 B 파트)
    """
    running_id = models.BigAutoField(primary_key=True)
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="running_sessions",
        verbose_name="사용자"
    )

    start_time = models.DateTimeField(verbose_name="시작 시간")
    end_time = models.DateTimeField(verbose_name="종료 시간")

    distance_km = models.DecimalField(max_digits=6, decimal_places=3, verbose_name="달린 거리(km)")
    total_time_seconds = models.PositiveIntegerField(verbose_name="총 경과 시간(초)")
    avg_pace_seconds_per_km = models.PositiveIntegerField(verbose_name="평균 페이스(초/km)")

    GUIDE_CHOICES = [
        ('beginner', '입문'),
        ('elementary', '초급'),
        ('intermediate', '중급'),
        ('free', '자유'),
    ]
    guide_level = models.CharField(max_length=20, choices=GUIDE_CHOICES, verbose_name="가이드 난이도")

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "러닝 세션"
        verbose_name_plural = "러닝 세션 목록"
        ordering = ['-start_time']

    def __str__(self):
        return f"{self.user.email} - {self.distance_km}km"
