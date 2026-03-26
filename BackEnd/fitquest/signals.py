from django.db.models.signals import post_save
from django.dispatch import receiver
from .models import CustomUser, UserSettings, UserStreak

@receiver(post_save, sender=CustomUser)
def create_related(sender, instance, created, **kwargs):
    if created:
        UserSettings.objects.get_or_create(user=instance)
        UserStreak.objects.get_or_create(user=instance)
