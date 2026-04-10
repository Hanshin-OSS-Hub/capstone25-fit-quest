#!/bin/bash
set -e

PROJECT_DIR=/home/ubuntu/capstone25-fit-quest
BRANCH=main
INFRA_DIR=$PROJECT_DIR/Infra

echo "===== DEPLOY START ====="

echo "[1] move to project dir"
cd "$PROJECT_DIR"

echo "[2] fetch latest code from origin"
git fetch origin

echo "[3] checkout branch"
git checkout "$BRANCH"

echo "[4] force sync local branch with origin/$BRANCH"
git reset --hard "origin/$BRANCH"

echo "[5] remove untracked files"
git clean -fd

echo "[6] move to infra dir"
cd "$INFRA_DIR"

echo "[7] stop old containers"
docker compose down

echo "[8] rebuild containers"
docker compose build

echo "[9] start containers"
docker compose up -d

echo "[10] cleanup unused docker images"
docker image prune -af

echo "[11] cleanup docker build cache"
docker builder prune -af

echo "===== DEPLOY SUCCESS ====="
