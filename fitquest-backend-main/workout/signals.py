from django.db.models.signals import post_save
from django.dispatch import receiver
from .models import RunningSession
from .services import process_running_log  # 우리가 만든 전문가 호출

@receiver(post_save, sender=RunningSession)
def on_running_session_created(sender, instance, created, **kwargs):
    # 러닝 기록이 '새로 생성(created)' 되었을 때만 작동
    if created:
        # 서비스 파일에 있는 함수에게 처리를 넘김
        process_running_log(instance)