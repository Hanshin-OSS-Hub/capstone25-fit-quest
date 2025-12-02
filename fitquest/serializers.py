from django.contrib.auth import get_user_model
from rest_framework import serializers
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

User = get_user_model()

# --- 회원가입 ---
class SignupSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)

    class Meta:
        model = User
        fields = ("email", "nickname", "password")

    def create(self, validated_data):
        password = validated_data.pop("password")
        user = User(**validated_data)
        user.set_password(password)
        user.save()
        return user

# --- 내 정보 응답 ---
class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'email', 'nickname', 'exp', 'point']

class EmailTokenObtainPairSerializer(TokenObtainPairSerializer):
    username_field = "email"

    def validate(self, attrs):
        email = attrs.get("email")
        password = attrs.get("password")

        user = User.objects.filter(email=email).first()
        if user is None:
            raise serializers.ValidationError("잘못된 이메일 또는 비밀번호입니다.")

        if not user.check_password(password):
            raise serializers.ValidationError("잘못된 이메일 또는 비밀번호입니다.")

        # 정상 로그인 → 부모 validate 호출해 JWT 발급
        data = super().validate(attrs)
        return data
    
# --- JWT 발급 유틸 ---
def issue_tokens_for_user(user: User) -> dict:
    refresh = RefreshToken.for_user(user)
    return {"access": str(refresh.access_token), "refresh": str(refresh)}
