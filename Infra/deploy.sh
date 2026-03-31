#!/bin/bash
set -e

PROJECT_DIR=/home/ubuntu/capstone25-fit-quest
BRANCH=main

echo "[1] move to project dir"
cd $PROJECT_DIR

echo "[2] fetch latest info"
git fetch origin

echo "[3] checkout branch"
git checkout $BRANCH

echo "[4] pull latest code"
git pull origin $BRANCH

echo "[5] move to infra dir"
cd $PROJECT_DIR/Infra

echo "[6] stop old containers"
docker compose down

echo "[7] rebuild containers"
docker compose build --no-cache

echo "[8] start containers"
docker compose up -d

echo "[9] done"
