# Python 베이스 이미지
FROM python:3.10-slim

# 작업 디렉토리 지정
WORKDIR /app

# 모든 파일 복사
COPY . /app

# pip 업그레이드 및 패키지 설치
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# Django 포트 개방
EXPOSE 8000

# 서버 실행 명령어
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]
