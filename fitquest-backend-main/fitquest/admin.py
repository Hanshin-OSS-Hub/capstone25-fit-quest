from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import CustomUser

class CustomUserAdmin(UserAdmin):
    model = CustomUser

    list_display = ("email", "nickname", "exp", "point", "is_staff")
    ordering = ("email",)  # ← username 대신 email 기준 정렬

    fieldsets = UserAdmin.fieldsets + (
        ("Profile", {"fields": ("nickname",)}),
        ("Stats", {"fields": ("exp", "point")}),
    )

admin.site.register(CustomUser, CustomUserAdmin)
