FROM python:3.10-slim

WORKDIR /app

# mysqlclient 빌드에 필요한 시스템 패키지
RUN apt-get update && apt-get install -y \
    default-mysql-client \
    gcc \
    default-libmysqlclient-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

COPY . .
EXPOSE 8000

CMD ["gunicorn", "config.wsgi:application", "--bind", "0.0.0.0:8000"]
