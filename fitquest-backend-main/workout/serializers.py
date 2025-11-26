from rest_framework import serializers
from .models import RunningSession, Quest, UserQuestProgress


class RunningSessionSerializer(serializers.ModelSerializer):
    class Meta:
        model = RunningSession
        fields = [
            "id",
            "distance_km",
            "duration_sec",
            "avg_pace_min_per_km",
            "current_pace_min_per_km",
            "calories_burned",
            "start_time",
            "end_time",
            "created_at",
        ]
        read_only_fields = ["id", "created_at"]


class QuestSerializer(serializers.ModelSerializer):
    class Meta:
        model = Quest
        fields = [
            "id",
            "title",
            "description",
            "quest_type",
            "target_value",
            "reward_xp",
            "reward_points",
            "is_active",
        ]


class UserQuestProgressSerializer(serializers.ModelSerializer):
    quest_name = serializers.CharField(source="quest.title", read_only=True)
    reward_xp = serializers.ReadOnlyField(source="quest.reward_xp")
    reward_points = serializers.ReadOnlyField(source="quest.reward_points")

    class Meta:
        model = UserQuestProgress
        fields = [
            "id",
            "quest",
            "quest_name",
            "progress_value",
            "is_completed",
            "completed_at",
            "reward_xp",
            "reward_points",
        ]
        read_only_fields = ["completed_at", "reward_xp", "reward_points"]
