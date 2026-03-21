from django.contrib import admin
from .models import Quest, UserQuestProgress, RunningSession

@admin.register(Quest)
class QuestAdmin(admin.ModelAdmin):
    list_display = ('title', 'quest_type', 'target_value', 'is_active', 'reward_xp')
    list_filter = ('quest_type', 'is_active')

@admin.register(UserQuestProgress)
class UserQuestProgressAdmin(admin.ModelAdmin):
    list_display = ('user', 'quest_title', 'progress_value', 'is_completed', 'cycle_key', 'updated_at')
    list_filter = ('is_completed', 'cycle_key')
    search_fields = ('user__email', 'quest__title')

    def quest_title(self, obj):
        return obj.quest.title

@admin.register(RunningSession)
class RunningSessionAdmin(admin.ModelAdmin):
    list_display = ('user', 'distance_km', 'duration_sec', 'start_time', 'created_at')
    list_filter = ('user', 'created_at')