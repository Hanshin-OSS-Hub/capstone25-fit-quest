from django.contrib import admin
# 모델 임포트 부분 합치기 
from .models import Quest, UserQuestProgress, RunningSession, Workout, WorkoutLog

# 1. 퀘스트 설정 (Quest)
@admin.register(Quest)
class QuestAdmin(admin.ModelAdmin):
    list_display = ('title', 'quest_type', 'target_value', 'is_active', 'reward_xp')
    list_filter = ('quest_type', 'is_active')

# 2. 유저 퀘스트 진행도 (UserQuestProgress)
@admin.register(UserQuestProgress)
class UserQuestProgressAdmin(admin.ModelAdmin):
    list_display = ('user', 'quest_title', 'progress_value', 'is_completed', 'cycle_key', 'updated_at')
    list_filter = ('is_completed', 'cycle_key')
    search_fields = ('user__email', 'quest__title')

    def quest_title(self, obj):
        return obj.quest.title

# 3. 러닝 세션 (RunningSession)
@admin.register(RunningSession)
class RunningSessionAdmin(admin.ModelAdmin):
    list_display = ('user', 'distance_km', 'duration_sec', 'start_time', 'created_at')
    list_filter = ('user', 'created_at')

# 4. 홈 트레이닝 운동 도감 (Workout)
@admin.register(Workout)
class WorkoutAdmin(admin.ModelAdmin):
    list_display = ('name', 'category', 'target_muscle', 'level', 'equipment')
    list_filter = ('category', 'level', 'target_muscle')
    search_fields = ('name',)

# 5. 유저 운동 기록 (WorkoutLog)
@admin.register(WorkoutLog)
class WorkoutLogAdmin(admin.ModelAdmin):
    list_display = ('user', 'workout', 'completed_at')