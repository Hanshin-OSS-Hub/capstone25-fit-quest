from django.db.models.signals import post_save
from django.dispatch import receiver
from .models import RunningSession, Quest, UserQuestProgress

@receiver(post_save, sender=RunningSession)
def update_quest_progress(sender, instance, created, **kwargs):
    """러닝 기록 생성 시 퀘스트 자동 업데이트"""
    if not created:
        return

    user = instance.user
    run_distance = float(instance.distance_km)

    quests = Quest.objects.filter(is_active=True)
    for quest in quests:
        progress, _ = UserQuestProgress.objects.get_or_create(
            user=user,
            quest=quest,
            defaults={"progress_value": 0, "is_completed": False}
        )

        # 아직 완료 안된 경우 진행 업데이트
        if not progress.is_completed:
            progress.progress_value += run_distance

            if progress.progress_value >= quest.target_value:
                progress.is_completed = True

            progress.save()
