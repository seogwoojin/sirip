# Dockerfile

FROM python:3.11-slim

WORKDIR /app

# 시스템 필수 패키지 (lightgbm 빌드용)
RUN apt-get update && apt-get install -y \
    build-essential \
    libgomp1 \
  && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

ENV PYTHONUNBUFFERED=1

EXPOSE 8000

CMD ["uvicorn", "api:app", "--host", "0.0.0.0", "--port", "8000"]
