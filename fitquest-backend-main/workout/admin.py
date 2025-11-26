# workout/admin.py

from django.contrib import admin
from .models import Quest, UserQuestProgress, RunningSession

admin.site.register(Quest)
admin.site.register(UserQuestProgress)
admin.site.register(RunningSession)
