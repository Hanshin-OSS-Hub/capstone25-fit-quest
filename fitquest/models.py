
# fitquest/models.py
from django.db import models
from django.contrib.auth.models import AbstractUser
from django.contrib.auth.base_user import BaseUserManager  # ★ 추가
from django.db.models.signals import post_save
from django.dispatch import receiver

#카카오
from django.conf import settings
from django.db import models
from django.utils import timezone

class SocialAccount(models.Model):
    PROVIDER_KAKAO = "kakao"

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="social_accounts",
        verbose_name="사용자",
    )
    provider = models.CharField(max_length=30, verbose_name="소셜 제공자")  # "kakao"
    provider_user_id = models.CharField(max_length=128, verbose_name="소셜 사용자 ID")
    email = models.EmailField(null=True, blank=True, verbose_name="소셜 이메일")
    nickname = models.CharField(max_length=100, blank=True, verbose_name="소셜 닉네임")
    connected_at = models.DateTimeField(default=timezone.now, verbose_name="연동 시각")

    class Meta:
        unique_together = ("provider", "provider_user_id")
        verbose_name = "소셜 계정"
        verbose_name_plural = "소셜 계정"

    def __str__(self):
        return f"{self.provider}:{self.provider_user_id} -> {self.user_id}"


class CustomUserManager(BaseUserManager):
    use_in_migrations = True

    def create_user(self, email, password=None, **extra_fields):
        if not email:
            raise ValueError("Email must be set")
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, email, password=None, **extra_fields):
        extra_fields.setdefault("is_staff", True)
        extra_fields.setdefault("is_superuser", True)
        extra_fields.setdefault("is_active", True)

        if extra_fields.get("is_staff") is not True:
            raise ValueError("Superuser must have is_staff=True.")
        if extra_fields.get("is_superuser") is not True:
            raise ValueError("Superuser must have is_superuser=True.")

        return self.create_user(email, password, **extra_fields)

class CustomUser(AbstractUser):
    username = None
    email = models.EmailField(unique=True)
    nickname = models.CharField(max_length=50, unique=True)

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["nickname"]   # createsuperuser 때 추가로 받는 필드

    objects = CustomUserManager()

    def __str__(self):
        return self.email

class UserProfile(models.Model):
    user = models.OneToOneField(CustomUser, on_delete=models.CASCADE, primary_key=True, related_name="profile")
    level = models.PositiveIntegerField(default=1)
    exp = models.PositiveIntegerField(default=0)
    points = models.PositiveIntegerField(default=0)

class UserSettings(models.Model):
    user = models.OneToOneField(CustomUser, on_delete=models.CASCADE, primary_key=True, related_name="settings")
    theme = models.CharField(max_length=10, default="light")
    notifications_on = models.BooleanField(default=True)

class UserStreak(models.Model):
    user = models.OneToOneField(CustomUser, on_delete=models.CASCADE, primary_key=True, related_name="streak")
    login_streak = models.PositiveIntegerField(default=0)
    quest_streak = models.PositiveIntegerField(default=0)

@receiver(post_save, sender=CustomUser)
def create_related(sender, instance, created, **kwargs):
    if created:
        UserProfile.objects.get_or_create(user=instance)
        UserSettings.objects.get_or_create(user=instance)
        UserStreak.objects.get_or_create(user=instance)
