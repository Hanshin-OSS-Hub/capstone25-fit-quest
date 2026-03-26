from django.apps import AppConfig


class WorkoutConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'workout'

    def ready(self):
       import workout.signals
