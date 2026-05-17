from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('workout', '0008_workout_calories_per_minute_and_more'),
    ]

    operations = [
        migrations.AddField(
            model_name='workout',
            name='image_name',
            field=models.CharField(blank=True, max_length=255, null=True),
        ),
    ]
